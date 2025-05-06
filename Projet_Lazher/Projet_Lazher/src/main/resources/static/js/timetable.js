document.addEventListener('DOMContentLoaded', function() {
    const calendarEl = document.getElementById('calendar');
    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'timeGridWeek',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'timeGridWeek,timeGridDay'
        },
        slotMinTime: '08:00:00',
        slotMaxTime: '20:00:00',
        allDaySlot: false,
        slotDuration: '00:30:00',
        height: 'auto',
        expandRows: true,
        nowIndicator: true,
        eventTimeFormat: {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        },
        slotLabelFormat: {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        },
        events: function(info, successCallback, failureCallback) {
            fetch('/api/timetable/courses')
                .then(response => response.json())
                .then(data => {
                    const events = data.map(course => ({
                        id: course.id,
                        title: `${course.title} (${course.section})`,
                        start: course.start,
                        end: course.end,
                        extendedProps: {
                            professor: course.professor,
                            room: course.room,
                            section: course.section
                        },
                        backgroundColor: getEventColor(course.section),
                        borderColor: getEventColor(course.section),
                        textColor: '#ffffff',
                        classNames: ['event-animation']
                    }));
                    successCallback(events);
                })
                .catch(error => {
                    console.error('Error fetching events:', error);
                    failureCallback(error);
                });
        },
        eventClick: function(info) {
            const event = info.event;
            const modal = `
                <div class="event-details">
                    <h4>${event.title}</h4>
                    <p><strong>Professor:</strong> ${event.extendedProps.professor}</p>
                    <p><strong>Room:</strong> ${event.extendedProps.room}</p>
                    <p><strong>Time:</strong> ${event.start.toLocaleString()} - ${event.end.toLocaleString()}</p>
                </div>
            `;
            
            // Create and show a custom modal
            const modalEl = document.createElement('div');
            modalEl.className = 'modal fade show';
            modalEl.style.display = 'block';
            modalEl.innerHTML = `
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">Course Details</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            ${modal}
                        </div>
                    </div>
                </div>
            `;
            document.body.appendChild(modalEl);
            
            // Add backdrop
            const backdrop = document.createElement('div');
            backdrop.className = 'modal-backdrop fade show';
            document.body.appendChild(backdrop);
            
            // Handle close
            const closeBtn = modalEl.querySelector('.btn-close');
            closeBtn.onclick = function() {
                modalEl.remove();
                backdrop.remove();
            };
        }
    });
    
    calendar.render();

    // Add event listeners for filters
    document.getElementById('sectionFilter').addEventListener('change', () => calendar.refetchEvents());
    document.getElementById('professorFilter').addEventListener('change', () => calendar.refetchEvents());
    document.getElementById('roomFilter').addEventListener('change', () => calendar.refetchEvents());
});

// Enhanced color scheme for sections
function getEventColor(section) {
    const colors = {
        'L1': '#e74c3c',      // Red
        'L2': '#3498db',      // Blue
        'L3': '#2ecc71',      // Green
        'M1': '#f1c40f',      // Yellow
        'M2': '#9b59b6',      // Purple
        'L1_INFO_SEC1': '#e74c3c',
        'L2_INFO': '#3498db',
        'L2_TIC': '#3498db',
        'L3_INFO': '#2ecc71',
        'L3_SE': '#2ecc71',
        'default': '#95a5a6'  // Gray
    };
    return colors[section] || colors.default;
} 