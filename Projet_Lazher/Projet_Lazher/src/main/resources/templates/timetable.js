document.addEventListener('DOMContentLoaded', function() {
    console.log('Initializing calendar...');
    const calendarEl = document.getElementById('calendar');
    
    if (!calendarEl) {
        console.error('Calendar element not found!');
        return;
    }
    
    try {
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
                console.log('Fetching events...');
                fetch('/api/courses')
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Network response was not ok: ' + response.statusText);
                        }
                        return response.json();
                    })
                    .then(data => {
                        console.log('Fetched courses:', data);
                        const events = data.map(course => {
                            // Extract day from description (format: "Day - Description")
                            let day = '';
                            let description = course.description || 'Unknown Course';
                            
                            if (course.description && course.description.includes(' - ')) {
                                const parts = course.description.split(' - ');
                                day = parts[0];
                                description = parts.slice(1).join(' - ');
                            }
                            
                            // Parse the date based on the day of the week
                            const today = new Date();
                            const currentDay = today.getDay(); // 0 = Sunday, 1 = Monday, etc.
                            let dayOffset = 0;
                            
                            switch(day.toLowerCase()) {
                                case 'lundi': dayOffset = 1 - currentDay; break;
                                case 'mardi': dayOffset = 2 - currentDay; break;
                                case 'mercredi': dayOffset = 3 - currentDay; break;
                                case 'jeudi': dayOffset = 4 - currentDay; break;
                                case 'vendredi': dayOffset = 5 - currentDay; break;
                                case 'samedi': dayOffset = 6 - currentDay; break;
                                default: dayOffset = 0;
                            }
                            
                            // If the day is in the past this week, schedule for next week
                            if (dayOffset < 0) dayOffset += 7;
                            
                            // Create date objects for start and end times
                            const startDate = new Date(today);
                            startDate.setDate(today.getDate() + dayOffset);
                            
                            const endDate = new Date(startDate);
                            
                            // Set hours and minutes from the course times
                            if (course.startTime) {
                                try {
                                    const startParts = course.startTime.split('T')[1].split(':');
                                    startDate.setHours(parseInt(startParts[0]), parseInt(startParts[1]), 0, 0);
                                } catch (e) {
                                    console.warn('Error parsing startTime:', course.startTime, e);
                                    // Default to 8:00 AM if parsing fails
                                    startDate.setHours(8, 0, 0, 0);
                                }
                            } else {
                                // Default to 8:00 AM if no startTime
                                startDate.setHours(8, 0, 0, 0);
                            }
                            
                            if (course.endTime) {
                                try {
                                    const endParts = course.endTime.split('T')[1].split(':');
                                    endDate.setHours(parseInt(endParts[0]), parseInt(endParts[1]), 0, 0);
                                } catch (e) {
                                    console.warn('Error parsing endTime:', course.endTime, e);
                                    // Default to startTime + 1.5 hours if parsing fails
                                    endDate.setTime(startDate.getTime() + (90 * 60 * 1000));
                                }
                            } else {
                                // Default to startTime + 1.5 hours if no endTime
                                endDate.setTime(startDate.getTime() + (90 * 60 * 1000));
                            }
                            
                            return {
                                id: course.id,
                                title: `${description} (${course.section || 'Default'})`,
                                start: startDate.toISOString(),
                                end: endDate.toISOString(),
                                extendedProps: {
                                    professor: course.professor ? course.professor.name : 'Unknown',
                                    room: course.room ? course.room.name : 'Unknown',
                                    section: course.section || 'Default',
                                    day: day
                                },
                                backgroundColor: getEventColor(course.section),
                                borderColor: getEventColor(course.section),
                                textColor: '#ffffff',
                                classNames: ['event-animation']
                            };
                        });
                        
                        console.log('Processed events:', events);
                        successCallback(events);
                    })
                    .catch(error => {
                        console.error('Error fetching events:', error);
                        failureCallback(error);
                    });
            },
            eventDidMount: function(info) {
                try {
                    // Add tooltips to events if Bootstrap is available
                    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
                        const tooltip = new bootstrap.Tooltip(info.el, {
                            title: `
                                <strong>${info.event.title}</strong><br>
                                Room: ${info.event.extendedProps.room}<br>
                                Professor: ${info.event.extendedProps.professor}<br>
                                Day: ${info.event.extendedProps.day}
                            `,
                            html: true,
                            placement: 'top',
                            container: 'body'
                        });
                        
                        // Add event listeners for tooltip
                        info.el.addEventListener('mouseenter', function() {
                            tooltip.show();
                        });
                        
                        info.el.addEventListener('mouseleave', function() {
                            tooltip.hide();
                        });
                    }
                } catch (e) {
                    console.warn('Error setting up tooltip:', e);
                }
            },
            eventClick: function(info) {
                try {
                    // Create and show modal using Bootstrap if available
                    const modalEl = document.getElementById('eventModal');
                    if (modalEl && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
                        const modalTitle = document.getElementById('modalTitle');
                        const modalBody = document.getElementById('modalBody');
                        
                        if (modalTitle && modalBody) {
                            modalTitle.textContent = info.event.title;
                            modalBody.innerHTML = `
                                <p><strong>Professor:</strong> ${info.event.extendedProps.professor}</p>
                                <p><strong>Room:</strong> ${info.event.extendedProps.room}</p>
                                <p><strong>Section:</strong> ${info.event.extendedProps.section}</p>
                                <p><strong>Day:</strong> ${info.event.extendedProps.day}</p>
                                <p><strong>Time:</strong> ${info.event.start.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} - 
                                    ${info.event.end.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</p>
                            `;
                            
                            const modal = new bootstrap.Modal(modalEl);
                            modal.show();
                        } else {
                            console.warn('Modal title or body elements not found');
                            alert(`${info.event.title}\nProfessor: ${info.event.extendedProps.professor}\nRoom: ${info.event.extendedProps.room}`);
                        }
                    } else {
                        // Fallback to alert if modal or bootstrap is not available
                        alert(`${info.event.title}\nProfessor: ${info.event.extendedProps.professor}\nRoom: ${info.event.extendedProps.room}`);
                    }
                } catch (e) {
                    console.warn('Error showing event details:', e);
                    alert(`${info.event.title}`);
                }
            }
        });
        
        calendar.render();
        console.log('Calendar rendered successfully');
        
        // Add event listeners for filters if they exist
        const sectionFilter = document.getElementById('sectionFilter');
        const professorFilter = document.getElementById('professorFilter');
        const roomFilter = document.getElementById('roomFilter');
        
        if (sectionFilter) {
            sectionFilter.addEventListener('change', () => calendar.refetchEvents());
        }
        
        if (professorFilter) {
            professorFilter.addEventListener('change', () => calendar.refetchEvents());
        }
        
        if (roomFilter) {
            roomFilter.addEventListener('change', () => calendar.refetchEvents());
        }
    } catch (e) {
        console.error('Error initializing calendar:', e);
    }
});

// Enhanced color scheme for sections
function getEventColor(section) {
    if (!section) return '#95a5a6'; // Default gray for null/undefined
    
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
        'Default': '#95a5a6'  // Gray
    };
    
    return colors[section] || colors['Default'];
}