/**
 * Main dashboard functionality for Health First Provider Portal
 */

document.addEventListener('DOMContentLoaded', () => {
  // Navigation functionality
  setupNavigation();
});

// Setup navigation between dashboard sections
function setupNavigation() {
  const navItems = document.querySelectorAll('.app-nav li');
  const sections = document.querySelectorAll('.content-section');
  
  navItems.forEach(item => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      
      // Get the target section from the href
      const link = item.querySelector('a');
      const targetId = link.getAttribute('href').substring(1);
      const targetSection = document.getElementById(`${targetId}-section`);
      
      // Update active nav item
      navItems.forEach(navItem => navItem.classList.remove('active'));
      item.classList.add('active');
      
      // Show target section, hide others
      sections.forEach(section => {
        section.classList.add('hidden');
      });
      
      if (targetSection) {
        targetSection.classList.remove('hidden');
      }
    });
  });
}