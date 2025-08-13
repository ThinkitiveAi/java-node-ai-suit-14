/**
 * Appointment management functionality
 */

document.addEventListener('DOMContentLoaded', () => {
  // Load appointments data
  loadAppointments();
  
  // Set up filter functionality
  const filterButton = document.getElementById('filter-appointments');
  if (filterButton) {
    filterButton.addEventListener('click', () => {
      const dateFilter = document.getElementById('appointment-date-filter').value;
      loadAppointments(dateFilter);
    });
  }
  
  // Clear filter functionality
  const clearFilterButton = document.getElementById('clear-filter');
  if (clearFilterButton) {
    clearFilterButton.addEventListener('click', () => {
      document.getElementById('appointment-date-filter').value = '';
      loadAppointments();
    });
  }
  
  // Set up navigation observers
  const navItems = document.querySelectorAll('.app-nav li a');
  navItems.forEach(item => {
    item.addEventListener('click', () => {
      if (item.getAttribute('href') === '#appointments') {
        // Refresh appointments when navigating to the section
        loadAppointments();
      }
    });
  });
});

// Load appointments from the API
async function loadAppointments(date = null) {
  try {
    const appointmentsList = document.getElementById('appointments-list');
    if (!appointmentsList) return;
    
    // Show loading state
    appointmentsList.innerHTML = '<p class="empty-state-message">Loading appointments...</p>';
    
    // Build URL with optional date filter
    let url = '/api/v1/appointments';
    if (date) {
      url += `?date=${date}`;
    }
    
    // Fetch appointments
    const response = await fetchWithAuth(url);
    
    if (!response.ok) {
      throw new Error('Failed to load appointments');
    }
    
    const data = await response.json();
    const appointments = data.appointments;
    
    // Clear container
    appointmentsList.innerHTML = '';
    
    if (!appointments || appointments.length === 0) {
      appointmentsList.innerHTML = '<p class="empty-state-message">No appointments found.</p>';
      return;
    }
    
    // Sort appointments by date and time
    appointments.sort((a, b) => {
      if (a.date !== b.date) {
        return new Date(a.date) - new Date(b.date);
      }
      return a.time.localeCompare(b.time);
    });
    
    // Group appointments by date
    const groupedByDate = {};
    appointments.forEach(appointment => {
      if (!groupedByDate[appointment.date]) {
        groupedByDate[appointment.date] = [];
      }
      groupedByDate[appointment.date].push(appointment);
    });
    
    // Create UI for each date group
    Object.keys(groupedByDate).sort().forEach(date => {
      const dateAppointments = groupedByDate[date];
      
      // Create date header
      const dateHeader = document.createElement('div');
      dateHeader.className = 'date-header';
      
      const formattedDate = new Date(date).toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
      
      dateHeader.innerHTML = `<h3>${formattedDate}</h3>`;
      appointmentsList.appendChild(dateHeader);
      
      // Create appointment cards for each appointment
      dateAppointments.forEach(appointment => {
        const appointmentCard = createAppointmentCard(appointment);
        appointmentsList.appendChild(appointmentCard);
      });
    });
  } catch (error) {
    console.error('Error loading appointments:', error);
    const appointmentsList = document.getElementById('appointments-list');
    if (appointmentsList) {
      appointmentsList.innerHTML = '<p class="empty-state-message">Failed to load appointments. Please try again later.</p>';
    }
  }
}

// Create an appointment card element
function createAppointmentCard(appointment) {
  const card = document.createElement('div');
  card.className = 'appointment-card';
  card.dataset.id = appointment.id;
  
  const header = document.createElement('div');
  header.className = 'appointment-header';
  
  const time = document.createElement('div');
  time.className = 'appointment-time';
  time.textContent = appointment.time;
  
  const status = document.createElement('div');
  status.className = `appointment-status ${appointment.status}`;
  status.textContent = appointment.status;
  
  header.appendChild(time);
  header.appendChild(status);
  
  const details = document.createElement('div');
  details.className = 'appointment-details';
  
  // Patient information
  if (appointment.patient) {
    const patientInfo = document.createElement('div');
    patientInfo.className = 'appointment-detail';
    
    const patientLabel = document.createElement('span');
    patientLabel.className = 'detail-label';
    patientLabel.textContent = 'Patient';
    
    const patientValue = document.createElement('span');
    patientValue.textContent = appointment.patient.name;
    
    patientInfo.appendChild(patientLabel);
    patientInfo.appendChild(patientValue);
    details.appendChild(patientInfo);
  }
  
  // Reason
  const reasonInfo = document.createElement('div');
  reasonInfo.className = 'appointment-detail';
  
  const reasonLabel = document.createElement('span');
  reasonLabel.className = 'detail-label';
  reasonLabel.textContent = 'Reason';
  
  const reasonValue = document.createElement('span');
  reasonValue.textContent = appointment.reason;
  
  reasonInfo.appendChild(reasonLabel);
  reasonInfo.appendChild(reasonValue);
  details.appendChild(reasonInfo);
  
  // Patient contact info if available
  if (appointment.patient) {
    const contactInfo = document.createElement('div');
    contactInfo.className = 'appointment-detail';
    
    const contactLabel = document.createElement('span');
    contactLabel.className = 'detail-label';
    contactLabel.textContent = 'Contact';
    
    const contactValue = document.createElement('span');
    contactValue.textContent = appointment.patient.phone || appointment.patient.email;
    
    contactInfo.appendChild(contactLabel);
    contactInfo.appendChild(contactValue);
    details.appendChild(contactInfo);
  }
  
  // Assemble the card
  card.appendChild(header);
  card.appendChild(details);
  
  // Add action buttons
  const actions = document.createElement('div');
  actions.className = 'appointment-actions';
  
  // Only allow actions for upcoming appointments
  const appointmentDate = new Date(`${appointment.date} ${appointment.time}`);
  if (appointmentDate > new Date() && appointment.status !== 'cancelled') {
    const cancelButton = document.createElement('button');
    cancelButton.className = 'text-button';
    cancelButton.innerHTML = '<span class="material-icons">cancel</span> Cancel';
    cancelButton.addEventListener('click', () => cancelAppointment(appointment.id));
    
    actions.appendChild(cancelButton);
  }
  
  card.appendChild(actions);
  
  return card;
}

// Cancel an appointment (placeholder)
async function cancelAppointment(appointmentId) {
  // This would normally call an API endpoint to cancel the appointment
  // For now, we'll just show a confirmation message
  if (confirm('This functionality is not yet implemented. In a real application, this would cancel the appointment. Do you want to refresh the list?')) {
    loadAppointments();
  }
}