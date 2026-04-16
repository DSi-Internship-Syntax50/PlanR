// Security Context
const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

const userRole = document.body.getAttribute('data-user-role') || 'GUEST';
const canEdit = (userRole === 'SUPERADMIN' || userRole === 'COORDINATOR');

// --- PURE SLOT CONFIGURATION ---
const MAX_SLOTS = 12; // Adjusted to match routine builder strictly by slots
const daysOrder = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY"];
const dayLabel = { "SUNDAY": "Sunday", "MONDAY": "Monday", "TUESDAY": "Tuesday", "WEDNESDAY": "Wednesday", "THURSDAY": "Thursday" };

let currentRoomId = null;
let currentRoomName = null;

// --- SMART PAGINATION VARIABLES ---
let allRooms = [];
let filteredRooms = [];
let currentPage = 1;
const roomsPerPage = 5;

document.addEventListener("DOMContentLoaded", () => {
    allRooms = Array.from(document.querySelectorAll('.room-card'));
    filteredRooms = [...allRooms];
    
    const searchInput = document.getElementById('roomSearchInput');
    const typeFilter = document.getElementById('typeFilter');
    
    if (searchInput) searchInput.addEventListener('input', applyFilters);
    if (typeFilter) typeFilter.addEventListener('change', applyFilters);

    applyFilters();
    bindRoomSelection();
});

function applyFilters() {
    const searchInput = document.getElementById('roomSearchInput');
    const typeFilterSelect = document.getElementById('typeFilter');
    
    if (!searchInput || !typeFilterSelect) return;

    const searchTerm = searchInput.value.toLowerCase();
    const typeFilter = typeFilterSelect.value;

    filteredRooms = allRooms.filter(card => {
        const text = card.innerText.toLowerCase();
        const typeBadge = card.querySelector('.room-type-badge');
        const type = typeBadge ? typeBadge.innerText.toUpperCase() : '';
        return text.includes(searchTerm) && (typeFilter === "" || type === typeFilter);
    });

    currentPage = 1; 
    renderPagination();
}

function renderPagination() {
    allRooms.forEach(c => c.style.display = 'none');
    
    const totalPages = Math.max(1, Math.ceil(filteredRooms.length / roomsPerPage));
    if (currentPage > totalPages) currentPage = totalPages; 

    const start = (currentPage - 1) * roomsPerPage;
    const toShow = filteredRooms.slice(start, start + roomsPerPage);
    toShow.forEach(c => c.style.display = 'block');
    
    const noRoomsMsg = document.getElementById('noRoomsMessage');
    const prevBtn = document.getElementById('prevPageBtn');
    const nextBtn = document.getElementById('nextPageBtn');
    
    if (noRoomsMsg) noRoomsMsg.style.display = filteredRooms.length === 0 ? 'block' : 'none';
    if (prevBtn) prevBtn.disabled = currentPage <= 1;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages;

    const pageNumbersDiv = document.getElementById('pageNumbers');
    if (pageNumbersDiv) {
        pageNumbersDiv.innerHTML = '';
        
        let startPage = Math.max(1, currentPage - 2);
        let endPage = Math.min(totalPages, startPage + 4);
        if (endPage - startPage < 4) startPage = Math.max(1, endPage - 4);

        for(let i = startPage; i <= endPage; i++) {
            const btn = document.createElement('button');
            btn.innerText = i;
            if (i === currentPage) {
                btn.className = `w-8 h-8 flex items-center justify-center rounded-lg text-xs font-extrabold bg-blue-600 text-white shadow-md shadow-blue-200/50 ring-2 ring-blue-600 ring-offset-1 transition-all flex-shrink-0`;
            } else {
                btn.className = `w-8 h-8 flex items-center justify-center rounded-lg text-xs font-semibold text-gray-500 hover:bg-blue-50 hover:text-blue-700 transition-colors flex-shrink-0`;
            }
            btn.onclick = () => goToPage(i);
            pageNumbersDiv.appendChild(btn);
        }
    }
}

function prevPage() { if(currentPage > 1) goToPage(currentPage - 1); }
function nextPage() { if(currentPage < Math.ceil(filteredRooms.length / roomsPerPage)) goToPage(currentPage + 1); }
function goToPage(p) { currentPage = p; renderPagination(); }

// --- ROOM SELECTION ---
function bindRoomSelection() {
    allRooms.forEach(card => {
        card.addEventListener("click", () => {
            allRooms.forEach(c => {
                c.classList.remove('border-blue-500', 'bg-blue-50/30', 'ring-1', 'ring-blue-500');
                c.classList.add('border-gray-100', 'bg-white');
                c.querySelector('.selected-badge')?.classList.add('hidden');
                c.querySelector('.room-title')?.classList.remove('text-blue-700');
                c.querySelector('.room-title')?.classList.add('text-gray-900');
            });

            card.classList.remove('border-gray-100', 'bg-white');
            card.classList.add('border-blue-500', 'bg-blue-50/30', 'ring-1', 'ring-blue-500');
            card.querySelector('.selected-badge')?.classList.remove('hidden');
            card.querySelector('.room-title')?.classList.remove('text-gray-900');
            card.querySelector('.room-title')?.classList.add('text-blue-700');

            currentRoomId = card.getAttribute("data-room-id");
            const floor = card.getAttribute("data-room-floor");
            const block = card.getAttribute("data-room-block");
            const rName = card.getAttribute("data-room-name");
            
            currentRoomName = `${floor && floor !== 'null' ? floor : ''}${block && block !== 'null' ? block : ''}${rName || ''}`;

            const titleElem = document.getElementById("selectedRoomTitle");
            const badgeElem = document.getElementById("roomStatusBadge");
            if (titleElem) titleElem.innerText = `${currentRoomName} Workspace`;
            if (badgeElem) {
                badgeElem.classList.remove("hidden");
                badgeElem.classList.add("flex");
            }

            loadRoomSchedule(currentRoomId);
        });
    });
}

// --- DYNAMIC CALENDAR LOGIC (12 SLOTS) ---
async function loadRoomSchedule(roomId) {
    const loading = document.getElementById("calendarLoading");
    if (loading) loading.classList.remove("hidden");

    try {
        const res = await fetch('/api/schedule/routines/room/' + roomId);
        const routines = await res.json();
        renderTimeline(routines);
    } catch (err) { console.error(err); } 
    finally { if (loading) loading.classList.add("hidden"); }
}

function renderTimeline(routines) {
    const headerDiv = document.getElementById("calendarHeader");
    const bodyDiv = document.getElementById("calendarBody");
    if (!headerDiv || !bodyDiv) return;
    
    const gridStyle = `grid-template-columns: 100px repeat(${MAX_SLOTS}, minmax(100px, 1fr)); display: grid;`;
    
    let headerHtml = `<div class="py-4 border-r border-gray-200 flex items-center justify-center"><i class="fas fa-calendar-day text-gray-400"></i></div>`;
    for(let i = 1; i <= MAX_SLOTS; i++) {
        headerHtml += `
            <div class="py-4 border-r border-gray-200 flex items-center justify-center font-bold text-gray-600">
                Slot ${i}
            </div>`;
    }
    headerDiv.style.cssText = gridStyle;
    headerDiv.className = "border-b border-gray-200 bg-gray-50 text-[11px] font-extrabold text-gray-500 uppercase tracking-widest text-center min-w-max p-2 gap-2";
    headerDiv.innerHTML = headerHtml;

    bodyDiv.style.cssText = gridStyle;
    bodyDiv.className = "flex-1 overflow-y-auto bg-gray-50/30 p-2 gap-2 min-w-max";
    bodyDiv.innerHTML = "";

    const matrix = {};
    for (let d of daysOrder) {
        matrix[d] = {};
        for (let s = 1; s <= MAX_SLOTS; s++) matrix[d][s] = null;
    }

    routines.forEach(r => {
        if (r.startSlotIndex && r.dayOfWeek && matrix[r.dayOfWeek]) {
            if (r.startSlotIndex <= MAX_SLOTS) {
                matrix[r.dayOfWeek][r.startSlotIndex] = r;
                if (r.slotCount > 1) {
                    for (let i = 1; i < r.slotCount; i++) {
                        if (r.startSlotIndex + i <= MAX_SLOTS) {
                            matrix[r.dayOfWeek][r.startSlotIndex + i] = "SPAN";
                        }
                    }
                }
            }
        }
    });

    let rowIndex = 1;
    for (let d of daysOrder) {
        bodyDiv.insertAdjacentHTML('beforeend', `
            <div class="flex items-center justify-center font-extrabold text-[11px] text-gray-500 bg-white rounded-xl shadow-sm border border-gray-100 uppercase tracking-widest" style="grid-column: 1; grid-row: ${rowIndex};">
                ${dayLabel[d]}
            </div>
        `);

        let colIndex = 2; // Slots start at col 2
        for (let s = 1; s <= MAX_SLOTS; s++) {
            let cellData = matrix[d][s];
            
            if (cellData === "SPAN") {
                // Handled by CSS Grid span
            } else if (cellData && cellData.id) {
                const courseCode = cellData.courseCode || 'TBA';
                const courseTitle = cellData.courseTitle || 'Untitled Course';
                const teacherName = cellData.teacherInitials || 'NA';
                const capacity = cellData.studentCapacity || '--';
                const batch = cellData.batch || 'N/A';
                
                const actualSpan = Math.min(cellData.slotCount, MAX_SLOTS - s + 1);

                const removeBtnHTML = canEdit ? `
                    <button onclick="unassignRoutine(${cellData.id})" class="absolute top-2 right-2 w-6 h-6 bg-red-100 text-red-600 rounded-md flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-600 hover:text-white" title="Unassign">
                        <i class="fas fa-trash-alt text-[10px]"></i>
                    </button>` : '';

                bodyDiv.insertAdjacentHTML('beforeend', `
                    <div class="allocation-block relative group" style="grid-row: ${rowIndex}; grid-column: ${colIndex} / span ${actualSpan};">
                        <div class="bg-white border-l-4 border-blue-600 rounded-xl p-3 h-full shadow-md flex flex-col justify-between overflow-hidden">
                            ${removeBtnHTML}
                            <div>
                                <div class="flex gap-2 items-center mb-1">
                                    <span class="bg-blue-100 text-blue-700 text-[9px] font-extrabold px-2 py-0.5 rounded uppercase">${courseCode}</span>
                                </div>
                                <p class="text-xs font-bold text-gray-900 leading-tight pr-6 mt-1 line-clamp-2" title="${courseTitle}">${courseTitle}</p>
                                <p class="text-[10px] text-gray-500 mt-1"><i class="fas fa-users mr-1"></i> Batch: ${batch}</p>
                            </div>
                            <div class="flex items-center gap-2 mt-3 pt-2 border-t border-gray-100">
                                <div class="w-6 h-6 rounded-full bg-gradient-to-tr from-gray-200 to-gray-300 text-gray-700 flex items-center justify-center text-[9px] font-bold shadow-sm" title="${teacherName}">${teacherName.substring(0, 2).toUpperCase()}</div>
                                <p class="text-[10px] text-gray-500 font-medium ml-auto">Cap: ${capacity}</p>
                            </div>
                        </div>
                    </div>
                `);
            } else {
                const addClickAttr = canEdit ? `onclick="openAllocationModal('${d}', ${s})"` : '';
                const cursorClass = canEdit ? 'cursor-pointer hover:border-blue-400 hover:bg-blue-50/50' : 'cursor-not-allowed opacity-50';
                const hoverContent = canEdit ? `
                    <div class="opacity-0 group-hover:opacity-100 transition flex flex-col items-center justify-center text-blue-500">
                        <i class="fas fa-plus-circle text-xl mb-1 drop-shadow-sm"></i>
                        <span class="text-[10px] font-bold uppercase tracking-widest text-blue-600">Assign</span>
                    </div>` : '';

                bodyDiv.insertAdjacentHTML('beforeend', `
                    <div class="group h-full w-full border-2 border-dashed border-gray-200 rounded-xl flex flex-col justify-center items-center transition bg-white/50 ${cursorClass}" ${addClickAttr} style="grid-row: ${rowIndex}; grid-column: ${colIndex};">
                        ${hoverContent}
                    </div>
                `);
            }
            colIndex++;
        }
        rowIndex++;
    }
}

async function unassignRoutine(routineId) {
    if(!confirm('Return this course to the unassigned pool?')) return;
    try {
        const fetchHeaders = { 'Content-Type': 'application/x-www-form-urlencoded' };
        if (csrfHeader && csrfToken) fetchHeaders[csrfHeader] = csrfToken;
        const res = await fetch('/api/schedule/unassign/' + routineId, { method: 'POST', headers: fetchHeaders });
        if (res.ok) loadRoomSchedule(currentRoomId);
        else alert("Failed to unassign.");
    } catch (err) { alert("Network error."); }
}

// --- CRUD MODALS: CREATE, EDIT, DELETE ---
async function deleteRoom(e, card) {
    if (e) e.stopPropagation(); 
    const roomId = card.getAttribute('data-room-id');
    const titleElem = card.querySelector('.room-title');
    const rName = titleElem ? titleElem.innerText : 'this room';
    if(!confirm(`Are you sure you want to permanently delete ${rName}?`)) return;

    try {
        const fetchHeaders = {};
        if (csrfHeader && csrfToken) fetchHeaders[csrfHeader] = csrfToken;
        const res = await fetch('/api/rooms/' + roomId, { method: 'DELETE', headers: fetchHeaders });
        if (res.ok) window.location.reload();
        else alert("Failed to delete. The room might have active courses assigned to it.");
    } catch(err) { alert("Network Error"); }
}

function openRoomModal(card = null, e = null) {
    if(e) e.stopPropagation();
    const form = document.getElementById("roomForm");
    if (!form) return;
    form.reset();
    const title = document.getElementById("roomModalTitle");

    if (card) {
        document.getElementById('editingRoomId').value = card.getAttribute('data-room-id');
        if (title) title.innerText = "Edit Facility Details";
        document.getElementById('r_roomNumber').value = card.getAttribute('data-room-name') || '';
        document.getElementById('r_type').value = card.getAttribute('data-room-type') || 'THEORY';
        document.getElementById('r_capacity').value = card.getAttribute('data-room-cap') || '';
        document.getElementById('r_floorNumber').value = card.getAttribute('data-room-floor') || '';
        document.getElementById('r_block').value = card.getAttribute('data-room-block') || '';
        document.getElementById('r_dept').value = card.getAttribute('data-room-dept-id') || '';
        document.getElementById('r_hasComputers').checked = card.getAttribute('data-has-comp') === 'true';
        document.getElementById('r_hasHardwareKits').checked = card.getAttribute('data-has-kits') === 'true';
    } else {
        document.getElementById('editingRoomId').value = "";
        if (title) title.innerText = "Create New Facility";
    }
    const modal = document.getElementById("roomModal");
    if (modal) {
        modal.classList.remove("hidden");
        modal.classList.add("flex");
    }
}

function closeRoomModal() {
    const modal = document.getElementById("roomModal");
    if (modal) {
        modal.classList.add("hidden");
        modal.classList.remove("flex");
    }
}

const rmForm = document.getElementById("roomForm");
if (rmForm) {
    rmForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const editingId = document.getElementById('editingRoomId').value;
        const payload = {
            roomNumber: document.getElementById("r_roomNumber").value,
            type: document.getElementById("r_type").value,
            capacity: document.getElementById("r_capacity").value ? parseInt(document.getElementById("r_capacity").value) : null,
            floorNumber: document.getElementById("r_floorNumber").value ? parseInt(document.getElementById("r_floorNumber").value) : null,
            block: document.getElementById("r_block").value,
            deptId: document.getElementById("r_dept").value ? parseInt(document.getElementById("r_dept").value) : null,
            hasComputers: document.getElementById("r_hasComputers").checked,
            hasHardwareKits: document.getElementById("r_hasHardwareKits").checked
        };

        const fetchHeaders = { 'Content-Type': 'application/json' };
        if (csrfHeader && csrfToken) fetchHeaders[csrfHeader] = csrfToken;

        try {
            const method = editingId ? 'PUT' : 'POST';
            const url = editingId ? `/api/rooms/${editingId}` : `/api/rooms`;
            const res = await fetch(url, { method: method, headers: fetchHeaders, body: JSON.stringify(payload) });
            if (res.ok) window.location.reload();
            else alert("Action failed. Check permissions.");
        } catch (e) { alert("Network error."); }
    });
}

// --- ALLOCATION MODAL LOGIC ---
let currentModalDay = null;
let currentModalSlot = null;

function openAllocationModal(day, slotIndex) {
    currentModalDay = day; currentModalSlot = slotIndex;
    const roomNameElem = document.getElementById("modalRoomName");
    const dayTimeElem = document.getElementById("modalDayTime");
    const modal = document.getElementById("allocationModal");

    if (roomNameElem) roomNameElem.innerText = currentRoomName;
    if (dayTimeElem) dayTimeElem.innerText = `On ${dayLabel[day]} starting at Slot ${slotIndex}`;
    if (modal) {
        modal.classList.remove("hidden");
        modal.classList.add("flex");
    }
    loadUnassignedRoutines();
}

function closeModal() {
    const modal = document.getElementById("allocationModal");
    if (modal) {
        modal.classList.add("hidden");
        modal.classList.remove("flex");
    }
}

async function loadUnassignedRoutines() {
    const listDiv = document.getElementById("unassignedList");
    if (!listDiv) return;
    listDiv.innerHTML = `<div class="text-center text-gray-500 py-8"><i class="fas fa-circle-notch fa-spin text-blue-500 text-2xl mb-2"></i><br>Scanning unassigned pool...</div>`;
    try {
        const res = await fetch('/api/schedule/routines/unassigned');
        const routines = await res.json();
        if (routines.length === 0) {
            listDiv.innerHTML = `<div class="text-center text-sm text-gray-500 py-8">🎉 All courses have been allocated!</div>`;
            return;
        }
        listDiv.innerHTML = routines.map(r => {
            const teacherName = r.teacherInitials || 'NA';
            return `
            <div class="p-4 bg-white border border-gray-100 rounded-xl shadow-sm flex justify-between items-center hover:border-blue-400 hover:shadow-md transition-all cursor-pointer group" onclick="allocateRoutine(${r.id})">
                <div>
                    <span class="bg-gray-100 text-gray-800 text-[10px] font-extrabold px-2 py-0.5 rounded uppercase group-hover:bg-blue-100 group-hover:text-blue-800 transition">${r.courseCode || 'TBA'}</span>
                    <div class="font-bold text-sm text-gray-900 group-hover:text-blue-700 transition mt-1">${r.courseTitle || 'Untitled'}</div>
                    <div class="text-[10px] text-gray-500 mt-1 font-medium"><i class="fas fa-chalkboard-teacher"></i> ${teacherName} &nbsp;|&nbsp; Cap: ${r.studentCapacity || '--'}</div>
                </div>
                <span class="text-[10px] text-gray-400 font-bold bg-gray-50 px-2 py-1 rounded"><i class="fas fa-clock"></i> ${r.slotCount} slots</span>
            </div>
        `}).join('');
    } catch (err) { listDiv.innerHTML = `<div class="text-center text-red-500 py-4">Error loading data.</div>`; }
}

async function allocateRoutine(routineId) {
    if (!currentRoomId) return;
    try {
        const formData = new URLSearchParams({ routineId: routineId, newDay: currentModalDay, newStartSlot: currentModalSlot, newRoomId: currentRoomId });
        const fetchHeaders = { 'Content-Type': 'application/x-www-form-urlencoded' };
        if (csrfHeader && csrfToken) fetchHeaders[csrfHeader] = csrfToken;

        const res = await fetch('/api/schedule/reschedule', { method: 'POST', headers: fetchHeaders, body: formData.toString() });
        if (res.ok) { closeModal(); loadRoomSchedule(currentRoomId); }
        else alert("Allocation Failed. Potential conflict detected.");
    } catch (error) { alert("Error during allocation."); }
}
