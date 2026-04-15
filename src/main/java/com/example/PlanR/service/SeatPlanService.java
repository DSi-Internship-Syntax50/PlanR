package com.example.PlanR.service;

import com.example.PlanR.dto.PoolDto;
import com.example.PlanR.dto.SeatPlanRequestDto;
import com.example.PlanR.dto.SeatPlanResponseDto;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeatPlanService {

    private int iterationCount = 0;
    private static final int MAX_ITERATIONS = 500000;

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
            return new SeatPlanResponseDto(false, null, "Total students (" + totalStudents + ") exceed capacity (" + (rows * cols) + "). Please reduce student count.");
        }

        // Fast fail for obvious impossible constraint
        if (maxCount > (Math.ceil(rows * cols / 2.0))) {
             return new SeatPlanResponseDto(false, null, "A single department has too many students to avoid adjacency. Please reduce the student count for that department.");
        }

        String[][] grid = new String[rows][cols];
        iterationCount = 0;
        boolean success = backtrack(grid, counts, 0, 0, rows, cols);

        if (success) {
            return new SeatPlanResponseDto(true, grid, "Optimal seat plan generated successfully.");
        } else {
             return new SeatPlanResponseDto(false, null, "Unable to find a seating arrangement without adjacency. Please reduce the count of students for the largest department.");
        }
    }

    private boolean backtrack(String[][] grid, Map<String, Integer> counts, int r, int c, int rows, int cols) {
        if (iterationCount++ > MAX_ITERATIONS) {
            return false;
        }
        
        if (r == rows) return true; // Filled all cells

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
                if (backtrack(grid, counts, nextR, nextC, rows, cols)) {
                    return true;
                }
                counts.put(code, counts.get(code) + 1);
                grid[r][c] = null;
            }
        }

        if (canBeEmpty) {
            grid[r][c] = null;
            if (backtrack(grid, counts, nextR, nextC, rows, cols)) {
                return true;
            }
        }

        return false;
    }

    private boolean isValid(String[][] grid, int r, int c, String code) {
        if (r > 0 && code.equals(grid[r - 1][c])) return false;
        else if (c > 0 && code.equals(grid[r][c - 1])) return false;
        return true;
    }
}
