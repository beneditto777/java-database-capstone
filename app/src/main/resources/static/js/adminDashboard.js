/*
  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form


  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')


  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors


  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards

    Call getDoctors() from the service layer
    Clear the current content area
    For each doctor returned:
    - Create a doctor card using createDoctorCard()
    - Append it to the content div

    Handle any fetch errors by logging them


  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()


  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty

    Read values from the search bar and filters
    Normalize empty values to null
    Call filterDoctors(name, time, specialty) from the service

    If doctors are found:
    - Render them using createDoctorCard()
    If no doctors match the filter:
    - Show a message: "No doctors found with the given filters."

    Catch and display any errors with an alert


  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it

    Clear the content area
    Loop through the doctors and append each card to the content area


  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/

import { openModal } from './components/modals.js';
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';

document.addEventListener("DOMContentLoaded", () => {
    // 1. Load initial doctor cards
    loadDoctorCards();

    // 2. Bind Add Doctor Button
    const addDocBtn = document.getElementById('addDocBtn');
    if (addDocBtn) {
        addDocBtn.addEventListener('click', () => {
            openModal('addDoctor');
        });
    }

    // 3. Bind Search and Filter Controls
    // Note: ensure IDs match the ones defined in adminDashboard.html
    const searchBar = document.getElementById("searchBar");
    const filterTime = document.getElementById("timeFilter");
    const filterSpecialty = document.getElementById("specialtyFilter");

    if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
    if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
    if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);
});

// Function to load all doctors on initial page load
async function loadDoctorCards() {
    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = "";

    try {
        const doctors = await getDoctors();
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Failed to load doctors:", error);
        contentDiv.innerHTML = "<p>Error loading doctors.</p>";
    }
}

// Function to handle filtering logic
async function filterDoctorsOnChange() {
    const searchBar = document.getElementById("searchBar").value.trim();
    const filterTime = document.getElementById("timeFilter").value;
    const filterSpecialty = document.getElementById("specialtyFilter").value;

    const name = searchBar.length > 0 ? searchBar : null;
    const time = filterTime.length > 0 ? filterTime : null;
    const specialty = filterSpecialty.length > 0 ? filterSpecialty : null;

    try {
        const response = await filterDoctors(name, time, specialty);
        // Depending on backend, it might return an array directly or { doctors: [...] }
        const doctors = response.doctors ? response.doctors : response;

        const contentDiv = document.getElementById("content");
        contentDiv.innerHTML = "";

        if (doctors && doctors.length > 0) {
            renderDoctorCards(doctors);
        } else {
            contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
        }
    } catch (error) {
        console.error("Failed to filter doctors:", error);
        document.getElementById("content").innerHTML = "<p>An error occurred while filtering doctors.</p>";
    }
}

// Utility function to render doctor cards
function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = "";

    if (!doctors || doctors.length === 0) return;

    doctors.forEach(doctor => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
    });
}

// Function to handle Add Doctor Form Submission
// Attached to window so the modal's inline onclick can reach it
window.adminAddDoctor = async function (event) {
    if (event) event.preventDefault();

    const token = localStorage.getItem("token");
    if (!token) {
        alert("Authentication error: Admin token missing. Please log in again.");
        window.location.href = "/";
        return;
    }

    // Grab values from modal inputs (Ensure these IDs match your modal HTML)
    const name = document.getElementById("docName").value;
    const email = document.getElementById("docEmail").value;
    const phone = document.getElementById("docPhone").value;
    const password = document.getElementById("docPassword").value;
    const specialty = document.getElementById("docSpecialty").value;

    // Collect available times from checkboxes
    const timeCheckboxes = document.querySelectorAll('input[name="availability"]:checked');
    const availableTimes = Array.from(timeCheckboxes).map(cb => cb.value);

    const doctor = { name, email, phone, password, specialty, availableTimes };

    try {
        const result = await saveDoctor(doctor, token);
        if (result.success) {
            alert(result.message);
            document.getElementById("modal").style.display = "none";
            loadDoctorCards(); // Refresh list
        } else {
            alert("Failed: " + result.message);
        }
    } catch (error) {
        console.error("Error adding doctor:", error);
        alert("An unexpected error occurred while adding the doctor.");
    }
};