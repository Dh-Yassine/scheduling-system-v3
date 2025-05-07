document.addEventListener('DOMContentLoaded', function() {
    console.log('Initializing special occasion calendar...');
    const calendarEl = document.getElementById('special-calendar');
    
    if (!calendarEl) {
        console.error('Special calendar element not found!');
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
        
        // Get the selected filters
        let selectedProfessorId = null;
        let selectedEventType = null;
        let selectedRoomId = null;
        
        // Check URL parameters first
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.has('professorId')) {
            selectedProfessorId = urlParams.get('professorId');
            console.log('Professor ID from URL:', selectedProfessorId);
            
            // Update the professor select if it exists
            const professorSelect = document.getElementById('professorFilter');
            if (professorSelect) {
                professorSelect.value = selectedProfessorId;
            }
        }
        
        // Initialize the calendar
        const calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'timeGridWeek',
            initialDate: startOfWeek, // Start the calendar on Monday of the current week
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek,timeGridDay'
            },
            slotMinTime: '08:00:00',
            slotMaxTime: '20:00:00',
            allDaySlot: true, // Allow all-day events for special occasions
            slotDuration: '00:30:00',
            height: 'auto',
            contentHeight: 600, // Set a fixed content height
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
                console.log('Fetching special events...');
                console.log('Calendar view range:', info.start, 'to', info.end);
                
                // Get filters
                selectedProfessorId = document.getElementById('professorFilter')?.value || 'all';
                selectedEventType = document.getElementById('eventTypeFilter')?.value || 'all';
                selectedRoomId = document.getElementById('roomFilter')?.value || 'all';
                
                // Determine the API endpoint
                let apiUrl = '/api/courses/special';
                if (selectedProfessorId && selectedProfessorId !== 'all') {
                    apiUrl = `/api/courses/professor/${selectedProfessorId}`;
                    console.log('Fetching special courses for professor:', selectedProfessorId);
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
                        console.log('Fetched special courses:', data);
                        
                        if (!data || data.length === 0) {
                            console.warn('No special courses found');
                            successCallback([]);
                            
                            // Update the list view
                            updateListView([]);
                            return;
                        }
                        
                        // Process the original events
                        const originalEvents = data.map(course => {
                            console.log('Processing special course:', course);
                            
                            // Extract day from description (format: "Day - Description")
                            let day = course.day || '';
                            let description = course.description || 'Unknown Course';
                            
                            if (course.description && course.description.includes(' - ')) {
                                const parts = course.description.split(' - ');
                                // Only update description, day comes from the DTO
                                description = parts.slice(1).join(' - ');
                            }
                            
                            // Create date objects from the ISO strings
                            let startDate, endDate;
                            
                            // Set start and end times from the course times
                            if (course.startTime) {
                                try {
                                    startDate = new Date(course.startTime);
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
                                    endDate = new Date(course.endTime);
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
                            
                            // Determine event type based on description
                            let eventType = 'other';
                            const descLower = description.toLowerCase();
                            if (descLower.includes('exam')) {
                                eventType = 'exam';
                            } else if (descLower.includes('conference')) {
                                eventType = 'conference';
                            } else if (descLower.includes('workshop')) {
                                eventType = 'workshop';
                            }
                            
                            // Filter by event type if selected
                            if (selectedEventType !== 'all' && eventType !== selectedEventType) {
                                return null;
                            }
                            
                            // Filter by room if selected
                            if (selectedRoomId !== 'all' && course.roomId !== selectedRoomId) {
                                return null;
                            }
                            
                            // Create the event data
                            return {
                                id: course.id,
                                title: `${description} (${course.section || 'Special'})`,
                                start: startDate,
                                end: endDate,
                                extendedProps: {
                                    professor: course.professorName || 'Unknown',
                                    room: course.roomName || 'Unknown',
                                    section: course.section || 'Special',
                                    day: day,
                                    eventType: eventType
                                },
                                backgroundColor: getEventColor(eventType),
                                borderColor: getEventColor(eventType),
                                textColor: '#ffffff',
                                classNames: ['event-animation']
                            };
                        }).filter(event => event !== null); // Remove filtered events
                        
                        console.log('Processed special events:', originalEvents);
                        
                        // Update the list view
                        updateListView(originalEvents);
                        
                        // Return the events for the calendar
                        successCallback(originalEvents);
                    })
                    .catch(error => {
                        console.error('Error fetching special events:', error);
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
                                Type: ${info.event.extendedProps.eventType}
                            `,
                            html: true,
                            placement: 'top',
                            container: 'body'
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
                                <p><strong>Type:</strong> ${info.event.extendedProps.eventType}</p>
                                <p><strong>Date:</strong> ${info.event.start.toLocaleDateString()}</p>
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
        console.log('Special calendar rendered successfully');
        
        // Add event listeners for filters
        const professorFilter = document.getElementById('professorFilter');
        const eventTypeFilter = document.getElementById('eventTypeFilter');
        const roomFilter = document.getElementById('roomFilter');
        
        if (professorFilter) {
            professorFilter.addEventListener('change', () => calendar.refetchEvents());
        }
        
        if (eventTypeFilter) {
            eventTypeFilter.addEventListener('change', () => calendar.refetchEvents());
        }
        
        if (roomFilter) {
            roomFilter.addEventListener('change', () => calendar.refetchEvents());
        }
    } catch (e) {
        console.error('Error initializing special calendar:', e);
    }
});

// Function to update the list view
function updateListView(events) {
    const listElement = document.getElementById('special-events-list');
    if (!listElement) return;
    
    if (!events || events.length === 0) {
        listElement.innerHTML = '<tr><td colspan="6" class="text-center">No special events found</td></tr>';
        return;
    }
    
    // Sort events by date
    events.sort((a, b) => new Date(a.start) - new Date(b.start));
    
    // Generate HTML for each event
    let html = '';
    events.forEach(event => {
        const startDate = new Date(event.start);
        const endDate = new Date(event.end);
        
        html += `
            <tr>
                <td>${event.title}</td>
                <td>${event.extendedProps.professor}</td>
                <td>${event.extendedProps.room}</td>
                <td>${startDate.toLocaleDateString()}</td>
                <td>${startDate.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} - 
                    ${endDate.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</td>
                <td>
                    <button class="btn btn-sm btn-primary view-event" data-event-id="${event.id}">
                        View
                    </button>
                </td>
            </tr>
        `;
    });
    
    listElement.innerHTML = html;
    
    // Add event listeners to the view buttons
    document.querySelectorAll('.view-event').forEach(button => {
        button.addEventListener('click', function() {
            const eventId = this.getAttribute('data-event-id');
            const event = events.find(e => e.id === eventId);
            
            if (event) {
                showEventDetails(event);
            }
        });
    });
}

// Function to show event details
function showEventDetails(event) {
    try {
        // Create and show modal using Bootstrap if available
        const modalEl = document.getElementById('eventModal');
        if (modalEl && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
            const modalTitle = document.getElementById('modalTitle');
            const modalBody = document.getElementById('modalBody');
            
            if (modalTitle && modalBody) {
                modalTitle.textContent = event.title;
                modalBody.innerHTML = `
                    <p><strong>Professor:</strong> ${event.extendedProps.professor}</p>
                    <p><strong>Room:</strong> ${event.extendedProps.room}</p>
                    <p><strong>Section:</strong> ${event.extendedProps.section}</p>
                    <p><strong>Type:</strong> ${event.extendedProps.eventType}</p>
                    <p><strong>Date:</strong> ${new Date(event.start).toLocaleDateString()}</p>
                    <p><strong>Time:</strong> ${new Date(event.start).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} - 
                        ${new Date(event.end).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</p>
                `;
                
                const modal = new bootstrap.Modal(modalEl);
                modal.show();
            } else {
                console.warn('Modal title or body elements not found');
                alert(`${event.title}\nProfessor: ${event.extendedProps.professor}\nRoom: ${event.extendedProps.room}`);
            }
        } else {
            // Fallback to alert if modal or bootstrap is not available
            alert(`${event.title}\nProfessor: ${event.extendedProps.professor}\nRoom: ${event.extendedProps.room}`);
        }
    } catch (e) {
        console.warn('Error showing event details:', e);
        alert(`${event.title}`);
    }
}

// Get color for event type
function getEventColor(eventType) {
    const colors = {
        'exam': '#e74c3c',      // Red
        'conference': '#3498db', // Blue
        'workshop': '#2ecc71',   // Green
        'other': '#9b59b6',      // Purple
        'Default': '#95a5a6'     // Gray
    };
    
    return colors[eventType] || colors['Default'];
}
