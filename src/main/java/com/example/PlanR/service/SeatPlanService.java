package com.example.PlanR.service;

import com.example.PlanR.dto.PoolDto;
import com.example.PlanR.dto.SeatPlanRequestDto;
import com.example.PlanR.dto.SeatPlanResponseDto;
import com.example.PlanR.model.Room;
import com.example.PlanR.model.SeatAllocation;
import com.example.PlanR.model.SeatPlan;
import com.example.PlanR.repository.RoomRepository;
import com.example.PlanR.repository.SeatPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for generating exam seat plans using backtracking.
 * FIXED: iterationCount is now method-local to ensure thread safety
 * (previously it was a mutable instance field in a Spring singleton).
 */
@Service
public class SeatPlanService {

    @Autowired
    private SeatPlanRepository seatPlanRepository;

    @Autowired
    private RoomRepository roomRepository;

    private static final int MAX_ITERATIONS = 500000;

    @Transactional
    public SeatPlanResponseDto generateSeatPlan(SeatPlanRequestDto request) {
        int rows = request.getRows();
        int cols = request.getCols();
        List<PoolDto> pools = request.getPools();

        if (rows <= 0 || cols <= 0) {
            return new SeatPlanResponseDto(false, null, "Invalid grid dimensions.");
        }
        if (pools == null || pools.isEmpty()) {
            return new SeatPlanResponseDto(false, null, "No student pools provided.");
        }

        int totalStudents = 0;
        Map<String, Integer> counts = new HashMap<>();
        int maxCount = 0;
        for (PoolDto pool : pools) {
            if (pool.getCount() > 0) {
                totalStudents += pool.getCount();
                counts.put(pool.getShortCode(), pool.getCount());
                maxCount = Math.max(maxCount, pool.getCount());
            }
        }

        if (totalStudents > rows * cols) {
            return new SeatPlanResponseDto(false, null, "Total students (" + totalStudents + ") exceed capacity ("
                    + (rows * cols) + "). Please reduce student count.");
        }

        // Fast fail for obvious impossible constraint
        if (maxCount > (Math.ceil(rows * cols / 2.0))) {
            return new SeatPlanResponseDto(false, null,
                    "A single department has too many students to avoid adjacency. Please reduce the student count for that department.");
        }

        String[][] grid = new String[rows][cols];
        // Thread-safe: iteration counter is now stack-local instead of instance field
        int[] iterationCount = { 0 };
        boolean success = backtrack(grid, counts, 0, 0, rows, cols, iterationCount);

        if (success) {
            // NEW: Persist to DB
            SeatPlan plan = new SeatPlan();
            plan.setGridRows(rows);
            plan.setGridCols(cols);

            if (request.getTargetRoomId() != null) {
                Room room = roomRepository.findById(request.getTargetRoomId()).orElse(null);
                plan.setRoom(room);
            }

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    SeatAllocation alloc = new SeatAllocation();
                    alloc.setRowIndex(r);
                    alloc.setColIndex(c);
                    alloc.setDepartmentCode(grid[r][c]); // Can be null
                    plan.addAllocation(alloc);
                }
            }

            plan = seatPlanRepository.save(plan);

            SeatPlanResponseDto response = new SeatPlanResponseDto(true, grid,
                    "Optimal seat plan generated successfully.");
            response.setSeatPlanId(plan.getId()); // Pass the ID to frontend
            return response;
        } else {
            return new SeatPlanResponseDto(false, null, "Unable to find a seating arrangement without adjacency.");
        }
    }

    private boolean backtrack(String[][] grid, Map<String, Integer> counts, int r, int c, int rows, int cols,
            int[] iterationCount) {
        if (iterationCount[0]++ > MAX_ITERATIONS) {
            return false;
        }

        if (r == rows)
            return true; // Filled all cells

        int nextR = c == cols - 1 ? r + 1 : r;
        int nextC = c == cols - 1 ? 0 : c + 1;

        // at (0,0), remaining = rows*cols. at (r,c), index = r*cols + c.
        int currentIndex = r * cols + c;
        int remainingCellsProper = (rows * cols) - currentIndex;

        int remainingStudents = counts.values().stream().mapToInt(Integer::intValue).sum();

        // Sort departments by remaining count descending to place highest counts first
        List<Map.Entry<String, Integer>> sortedDepts = counts.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .toList();

        boolean canBeEmpty = remainingCellsProper > remainingStudents;

        for (Map.Entry<String, Integer> dept : sortedDepts) {
            String code = dept.getKey();
            if (isValid(grid, r, c, code)) {
                grid[r][c] = code;
                counts.put(code, counts.get(code) - 1);
                if (backtrack(grid, counts, nextR, nextC, rows, cols, iterationCount)) {
                    return true;
                }
                counts.put(code, counts.get(code) + 1);
                grid[r][c] = null;
            }
        }

        if (canBeEmpty) {
            grid[r][c] = null;
            if (backtrack(grid, counts, nextR, nextC, rows, cols, iterationCount)) {
                return true;
            }
        }

        return false;
    }

    private boolean isValid(String[][] grid, int r, int c, String code) {
        if (r > 0 && code.equals(grid[r - 1][c]))
            return false;
        else if (c > 0 && code.equals(grid[r][c - 1]))
            return false;
        return true;
    }

    @Transactional(readOnly = true)
    public SeatPlanResponseDto getSeatPlanForRoom(Long roomId) {
        return seatPlanRepository.findTopByRoomIdOrderByGeneratedAtDesc(roomId)
                .map(plan -> {
                    String[][] grid = new String[plan.getGridRows()][plan.getGridCols()];
                    for (SeatAllocation alloc : plan.getAllocations()) {
                        grid[alloc.getRowIndex()][alloc.getColIndex()] = alloc.getDepartmentCode();
                    }
                    SeatPlanResponseDto response = new SeatPlanResponseDto(true, grid, "Loaded saved seat plan.");
                    response.setSeatPlanId(plan.getId());
                    return response;
                })
                .orElse(new SeatPlanResponseDto(false, null, "No saved plan found for this room."));
    }
}
