/*
  Import getAllAppointments to fetch appointments from the backend
  Import createPatientRow to generate a table row for each patient appointment


  Get the table body where patient rows will be added
  Initialize selectedDate with today's date in 'YYYY-MM-DD' format
  Get the saved token from localStorage (used for authenticated API calls)
  Initialize patientName to null (used for filtering by name)


  Add an 'input' event listener to the search bar
  On each keystroke:
    - Trim and check the input value
    - If not empty, use it as the patientName for filtering
    - Else, reset patientName to "null" (as expected by backend)
    - Reload the appointments list with the updated filter


  Add a click listener to the "Today" button
  When clicked:
    - Set selectedDate to today's date
    - Update the date picker UI to match
    - Reload the appointments for today


  Add a change event listener to the date picker
  When the date changes:
    - Update selectedDate with the new value
    - Reload the appointments for that specific date


  Function: loadAppointments
  Purpose: Fetch and display appointments based on selected date and optional patient name

  Step 1: Call getAllAppointments with selectedDate, patientName, and token
  Step 2: Clear the table body content before rendering new rows

  Step 3: If no appointments are returned:
    - Display a message row: "No Appointments found for today."

  Step 4: If appointments exist:
    - Loop through each appointment and construct a 'patient' object with id, name, phone, and email
    - Call createPatientRow to generate a table row for the appointment
    - Append each row to the table body

  Step 5: Catch and handle any errors during fetch:
    - Show a message row: "Error loading appointments. Try again later."


  When the page is fully loaded (DOMContentLoaded):
    - Call renderContent() (assumes it sets up the UI layout)
    - Call loadAppointments() to display today's appointments by default
*/

import { getAllAppointments } from './services/appointmentRecordService.js';
import { createPatientRow } from './components/patientRows.js';

// Global Variables
const token = localStorage.getItem("token");
let selectedDate = new Date().toISOString().split('T')[0]; // Format: YYYY-MM-DD
let patientName = null;

document.addEventListener("DOMContentLoaded", () => {
    // Elements
    const datePicker = document.getElementById("datePicker");
    const searchBar = document.getElementById("searchBar");
    const todayButton = document.getElementById("todayButton");

    // 1. Initialize Date Picker UI
    if (datePicker) {
        datePicker.value = selectedDate;
    }

    // 2. Initial Data Load
    loadAppointments();

    // 3. Search Bar Listener
    if (searchBar) {
        searchBar.addEventListener("input", (e) => {
            const val = e.target.value.trim();
            patientName = val.length > 0 ? val : "null";
            loadAppointments();
        });
    }

    // 4. "Today's Appointments" Listener
    if (todayButton) {
        todayButton.addEventListener("click", () => {
            selectedDate = new Date().toISOString().split('T')[0];
            if (datePicker) datePicker.value = selectedDate;
            loadAppointments();
        });
    }

    // 5. Date Picker Listener
    if (datePicker) {
        datePicker.addEventListener("change", (e) => {
            selectedDate = e.target.value;
            loadAppointments();
        });
    }
});

// Function to fetch and render appointments
async function loadAppointments() {
    const tbody = document.getElementById("patientTableBody");
    if (!tbody) return;

    // Clear existing table content
    tbody.innerHTML = "";

    try {
        // Fetch appointments using service
        const appointments = await getAllAppointments(selectedDate, patientName, token);

        // Handle empty states
        if (!appointments || appointments.length === 0) {
            const tr = document.createElement("tr");
            tr.innerHTML = `<td colspan="5" class="noPatientRecord">No Appointments found for the selected date.</td>`;
            tbody.appendChild(tr);
            return;
        }

        // Render rows
        appointments.forEach(appointment => {
            const row = createPatientRow(appointment);
            tbody.appendChild(row);
        });

    } catch (error) {
        console.error("Error loading appointments:", error);
        const tr = document.createElement("tr");
        tr.innerHTML = `<td colspan="5" class="noPatientRecord">Error loading appointments. Try again later.</td>`;
        tbody.appendChild(tr);
    }
}