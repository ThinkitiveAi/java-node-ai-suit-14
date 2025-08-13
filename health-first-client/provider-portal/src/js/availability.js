/**
 * Provider availability management functionality
 */

document.addEventListener('DOMContentLoaded', () => {
  // Load availability data
  loadAvailabilityData();
  
  // Save button event listener
  const saveButton = document.getElementById('save-availability');
  if (saveButton) {
    saveButton.addEventListener('click', saveAvailability);
  }
});

// Load provider availability data from API
async function loadAvailabilityData() {
  try {
    const container = document.getElementById('availability-container');
    if (!container) return;
    
    // Show loading state
    container.innerHTML = '<p class="empty-state-message">Loading availability...</p>';
    
    // Fetch provider availability
    const response = await fetchWithAuth('/api/v1/provider/availability');
    
    if (!response.ok) {
      throw new Error('Failed to load availability data');
    }
    
    const data = await response.json();
    
    // Clear container
    container.innerHTML = '';
    
    // Define standard time slots
    const standardSlots = [
      '08:00', '08:30', '09:00', '09:30', '10:00', '10:30', '11:00', '11:30',
      '12:00', '12:30', '13:00', '13:30', '14:00', '14:30', '15:00', '15:30',
      '16:00', '16:30', '17:00', '17:30'
    ];
    
    // Define days of week
    const daysOfWeek = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];
    
    // Create availability UI for each day
    daysOfWeek.forEach(day => {
      // Find day in availability data
      const dayData = data.availability.find(a => a.day === day) || { day, slots: [] };
      
      // Create day card
      const dayCard = document.createElement('div');
      dayCard.className = 'day-card';
      
      // Create day header
      const dayHeader = document.createElement('div');
      dayHeader.className = 'day-header';
      
      const dayTitle = document.createElement('span');
      dayTitle.textContent = day;
      
      const toggleButton = document.createElement('button');
      toggleButton.className = 'text-button';
      toggleButton.innerHTML = `<span class="material-icons">event_available</span>`;
      toggleButton.title = 'Toggle all slots';
      
      dayHeader.appendChild(dayTitle);
      dayHeader.appendChild(toggleButton);
      
      // Create time slots container
      const timeSlotsContainer = document.createElement('div');
      timeSlotsContainer.className = 'time-slots';
      
      // Create checkbox for each standard time slot
      standardSlots.forEach(time => {
        const isChecked = dayData.slots.includes(time);
        
        const timeSlot = document.createElement('div');
        timeSlot.className = 'time-slot';
        
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.id = `${day}-${time}`;
        checkbox.name = `${day}-${time}`;
        checkbox.value = time;
        checkbox.checked = isChecked;
        checkbox.dataset.day = day;
        checkbox.dataset.time = time;
        
        const label = document.createElement('label');
        label.htmlFor = `${day}-${time}`;
        label.textContent = time;
        
        timeSlot.appendChild(checkbox);
        timeSlot.appendChild(label);
        
        timeSlotsContainer.appendChild(timeSlot);
      });
      
      // Add toggle all functionality
      toggleButton.addEventListener('click', () => {
        const checkboxes = timeSlotsContainer.querySelectorAll('input[type="checkbox"]');
        
        // Check if all are checked or not
        const allChecked = Array.from(checkboxes).every(cb => cb.checked);
        
        // Toggle all
        checkboxes.forEach(cb => {
          cb.checked = !allChecked;
        });
      });
      
      // Assemble day card
      dayCard.appendChild(dayHeader);
      dayCard.appendChild(timeSlotsContainer);
      
      // Add to container
      container.appendChild(dayCard);
    });
  } catch (error) {
    console.error('Error loading availability:', error);
    const container = document.getElementById('availability-container');
    if (container) {
      container.innerHTML = '<p class="empty-state-message">Failed to load availability. Please try again later.</p>';
    }
  }
}

// Save provider availability data
async function saveAvailability() {
  try {
    // Get the save button and show loading state
    const saveButton = document.getElementById('save-availability');
    const originalButtonText = saveButton.textContent;
    saveButton.disabled = true;
    saveButton.textContent = 'Saving...';
    
    // Collect availability data from checkboxes
    const daysOfWeek = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];
    const availability = [];
    
    // For each day, collect checked time slots
    daysOfWeek.forEach(day => {
      const dayCard = document.querySelector(`.day-card:has(span:contains("${day}"))`);
      if (!dayCard) return;
      
      const checkboxes = dayCard.querySelectorAll('input[type="checkbox"]:checked');
      const slots = Array.from(checkboxes).map(cb => cb.value);
      
      availability.push({ day, slots });
    });
    
    // Send updated availability to API
    const response = await fetchWithAuth('/api/v1/provider/availability', {
      method: 'PUT',
      body: JSON.stringify({ availability })
    });
    
    if (!response.ok) {
      throw new Error('Failed to save availability data');
    }
    
    // Show success feedback
    saveButton.textContent = 'Saved!';
    saveButton.classList.add('success');
    
    // Reset button state after a delay
    setTimeout(() => {
      saveButton.disabled = false;
      saveButton.textContent = originalButtonText;
      saveButton.classList.remove('success');
    }, 2000);
  } catch (error) {
    console.error('Error saving availability:', error);
    alert('Failed to save availability. Please try again.');
    
    // Reset button state
    const saveButton = document.getElementById('save-availability');
    saveButton.disabled = false;
    saveButton.textContent = 'Save Changes';
  }
}

// Polyfill for :has selector since it might not be supported in all browsers
if (!document.querySelector(':has(*)')) {
  document.querySelectorAll = function(selector) {
    if (selector.includes(':has')) {
      // Extract the day from the selector
      const dayMatch = selector.match(/:contains\("([^"]+)"\)/);
      if (dayMatch && dayMatch[1]) {
        const day = dayMatch[1];
        const dayCards = document.querySelectorAll('.day-card');
        return Array.from(dayCards).filter(card => {
          const span = card.querySelector('span');
          return span && span.textContent === day;
        });
      }
    }
    
    // Default implementation for other selectors
    return document.querySelectorAll(selector);
  };
}