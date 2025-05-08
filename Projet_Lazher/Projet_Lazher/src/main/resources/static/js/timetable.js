document.addEventListener('DOMContentLoaded', function() {
    console.log('Initializing calendar...');
    const calendarEl = document.getElementById('calendar');
    
    if (!calendarEl) {
        console.error('Calendar element not found!');
        return;
    }
    
    try {
        // Get the current date
        const today = new Date();
        
        // Calculate the start of the current week (Monday)
        const startOfWeek = new Date(today);
        const dayOfWeek = today.getDay(); // 0 = Sunday, 1 = Monday, etc.
        const diff = dayOfWeek === 0 ? -6 : 1 - dayOfWeek; // Adjust to get Monday
        startOfWeek.setDate(today.getDate() + diff);
        
        console.log('Start of week:', startOfWeek);
        
        // Get filter parameters from URL
        const urlParams = new URLSearchParams(window.location.search);
        let selectedProfessorId = null;
        let selectedSection = null;
        
        // Check for professor ID in URL
        if (urlParams.has('professorId')) {
            selectedProfessorId = urlParams.get('professorId');
            console.log('Professor ID from URL:', selectedProfessorId);
            
            // Update the professor select if it exists
            const professorSelect = document.getElementById('professorEmailSelect');
            if (professorSelect) {
                professorSelect.value = selectedProfessorId;
            }
        }
        
        // Check for section in URL
        if (urlParams.has('section')) {
            selectedSection = urlParams.get('section');
            console.log('Section from URL:', selectedSection);
            
            // Update the section select if it exists
            const sectionSelect = document.getElementById('sectionFilter');
            if (sectionSelect) {
                sectionSelect.value = selectedSection;
            }
        }
        
        const calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'timeGridWeek',
            initialDate: startOfWeek, // Start the calendar on Monday of the current week
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
            contentHeight: 450, // Increased content height to match container
            expandRows: true,
            nowIndicator: true,
            firstDay: 1, // Start the week on Monday (0 = Sunday, 1 = Monday)
            handleWindowResize: true,
            stickyHeaderDates: true,
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
                console.log('Calendar view range:', info.start, 'to', info.end);
                
                // Determine the API endpoint based on filters
                let apiUrl = '/api/courses';
                
                // Professor filter takes precedence over section filter
                if (selectedProfessorId && selectedProfessorId !== 'all') {
                    apiUrl = `/api/courses/professor/${selectedProfessorId}`;
                    console.log('Fetching courses for professor:', selectedProfessorId);
                } 
                // If no professor selected but section is selected
                else if (selectedSection && selectedSection !== 'all') {
                    apiUrl = `/api/courses/section/${selectedSection}`;
                    console.log('Fetching courses for section:', selectedSection);
                }
                
                fetch(apiUrl)
                    .then(response => {
                        console.log('Response status:', response.status);
                        if (!response.ok) {
                            throw new Error('Network response was not ok: ' + response.statusText);
                        }
                        return response.json();
                    })
                    .then(data => {
                        console.log('Fetched courses:', data);
                        
                        if (!data || data.length === 0) {
                            console.warn('No courses found in the database');
                            successCallback([]);
                            return;
                        }
                        
                        // Process the original events
                        const originalEvents = data.map(course => {
                            console.log('Processing course:', course);
                            
                            // Extract day from description (format: "Day - Description")
                            let day = course.day || '';
                            let description = course.description || 'Unknown Course';
                            
                            if (course.description && course.description.includes(' - ')) {
                                const parts = course.description.split(' - ');
                                // Only update description, day comes from the DTO
                                description = parts.slice(1).join(' - ');
                            }
                            
                            console.log('Using day:', day, 'description:', description);
                            
                            // Create date objects from the ISO strings
                            let startDate, endDate;
                            
                            // Set start and end times from the course times
                            if (course.startTime) {
                                try {
                                    console.log('Start time:', course.startTime);
                                    startDate = new Date(course.startTime);
                                    console.log('Parsed start date:', startDate, 'Day:', startDate.getDay(), 'Hours:', startDate.getHours());
                                } catch (e) {
                                    console.warn('Error parsing startTime:', course.startTime, e);
                                    // Default to current date at 8:00 AM
                                    startDate = new Date();
                                    startDate.setHours(8, 0, 0, 0);
                                }
                            } else {
                                // Default to current date at 8:00 AM
                                startDate = new Date();
                                startDate.setHours(8, 0, 0, 0);
                                console.warn('No start time provided for course:', course);
                            }
                            
                            if (course.endTime) {
                                try {
                                    console.log('End time:', course.endTime);
                                    endDate = new Date(course.endTime);
                                    console.log('Parsed end date:', endDate, 'Day:', endDate.getDay(), 'Hours:', endDate.getHours());
                                } catch (e) {
                                    console.warn('Error parsing endTime:', course.endTime, e);
                                    // Default to startTime + 1.5 hours
                                    endDate = new Date(startDate.getTime() + (90 * 60 * 1000));
                                }
                            } else {
                                // Default to startTime + 1.5 hours
                                endDate = new Date(startDate.getTime() + (90 * 60 * 1000));
                                console.warn('No end time provided for course:', course);
                            }
                            
                            // Create the event data
                            return {
                                id: course.id,
                                title: `${description} (${course.section || 'Default'})`,
                                start: startDate,
                                end: endDate,
                                extendedProps: {
                                    professor: course.professorName || 'Unknown',
                                    room: course.roomName || 'Unknown',
                                    section: course.section || 'Default',
                                    day: day
                                },
                                backgroundColor: getEventColor(course.section),
                                borderColor: getEventColor(course.section),
                                textColor: '#ffffff',
                                classNames: ['event-animation'],
                                // Add recurrence rule to make events repeat weekly
                                daysOfWeek: [startDate.getDay()],
                                startTime: startDate.toTimeString().substring(0, 5),
                                endTime: endDate.toTimeString().substring(0, 5),
                                startRecur: info.start,
                                endRecur: null // No end date for recurrence
                            };
                        });
                        
                        console.log('Processed events:', originalEvents);
                        
                        // Adapt events to the current view range
                        const adaptedEvents = [];
                        
                        originalEvents.forEach(event => {
                            // Get the day of week (0-6)
                            const dayOfWeek = new Date(event.start).getDay();
                            
                            // Get the time part (hours and minutes)
                            const startHours = new Date(event.start).getHours();
                            const startMinutes = new Date(event.start).getMinutes();
                            const endHours = new Date(event.end).getHours();
                            const endMinutes = new Date(event.end).getMinutes();
                            
                            // Create a new event for the current view range
                            const viewStart = new Date(info.start);
                            const viewEnd = new Date(info.end);
                            
                            // Iterate through each day in the view range
                            for (let day = new Date(viewStart); day < viewEnd; day.setDate(day.getDate() + 1)) {
                                // If this day matches the event's day of week
                                if (day.getDay() === dayOfWeek) {
                                    // Create a new date with the correct day and time
                                    const eventStart = new Date(day);
                                    eventStart.setHours(startHours, startMinutes, 0, 0);
                                    
                                    const eventEnd = new Date(day);
                                    eventEnd.setHours(endHours, endMinutes, 0, 0);
                                    
                                    // Create a new event for this occurrence
                                    adaptedEvents.push({
                                        ...event,
                                        start: eventStart,
                                        end: eventEnd,
                                        id: `${event.id}-${day.toISOString().split('T')[0]}` // Unique ID for each occurrence
                                    });
                                }
                            }
                        });
                        
                        console.log('Adapted events for view range:', adaptedEvents);
                        successCallback(adaptedEvents);
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
        const professorFilter = document.getElementById('professorEmailSelect');
        
        if (sectionFilter) {
            sectionFilter.addEventListener('change', () => calendar.refetchEvents());
        }
        
        if (professorFilter) {
            professorFilter.addEventListener('change', () => calendar.refetchEvents());
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