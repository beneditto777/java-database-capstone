// Import the openModal function to handle showing login popups/modals
import { openModal } from '../components/modals.js';
// Import the base API URL from the config file
import { API_BASE_URL } from '../config/config.js';

// Define constants for the admin, doctor, and patient login API endpoints
const ADMIN_API = API_BASE_URL + '/admin/login';
const DOCTOR_API = API_BASE_URL + '/doctor/login';
const PATIENT_API = API_BASE_URL + '/patient/login';

// Use the window.onload event to ensure DOM elements are available
window.onload = function () {
    // Select the buttons from the landing page
    const adminBtn = document.getElementById('btn-admin');
    const doctorBtn = document.getElementById('btn-doctor');
    const patientBtn = document.getElementById('btn-patient');

    // Add a click event listener to show the admin login modal
    if (adminBtn) {
        adminBtn.addEventListener('click', () => {
            openModal('adminLogin');
        });
    }

    // Add a click event listener to show the doctor login modal
    if (doctorBtn) {
        doctorBtn.addEventListener('click', () => {
            openModal('doctorLogin');
        });
    }

    // Add a click event listener to show the patient login modal
    if (patientBtn) {
        patientBtn.addEventListener('click', () => {
            openModal('patientLogin');
        });
    }
};

/**
 * Handle Admin Login Submission
 */
window.adminLoginHandler = async function () {
    const usernameInput = document.getElementById('adminUsername');
    const passwordInput = document.getElementById('adminPassword');

    if (!usernameInput || !passwordInput) return;

    const username = usernameInput.value;
    const password = passwordInput.value;
    const admin = { username, password };

    try {
        const response = await fetch(ADMIN_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(admin)
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);

            if (typeof selectRole === 'function') {
                selectRole('admin');
            } else {
                localStorage.setItem('userRole', 'admin');
                window.location.href = '/pages/adminDashboard.html';
            }
        } else {
            alert("Invalid admin credentials!");
        }
    } catch (error) {
        console.error("Admin Login Error:", error);
        alert("An error occurred while trying to log in. Please try again later.");
    }
};

/**
 * Handle Doctor Login Submission
 */
window.doctorLoginHandler = async function () {
    const emailInput = document.getElementById('doctorEmail');
    const passwordInput = document.getElementById('doctorPassword');

    if (!emailInput || !passwordInput) return;

    const email = emailInput.value;
    const password = passwordInput.value;
    const doctor = { email, password };

    try {
        const response = await fetch(DOCTOR_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(doctor)
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);

            if (typeof selectRole === 'function') {
                selectRole('doctor');
            } else {
                localStorage.setItem('userRole', 'doctor');
                window.location.href = '/pages/doctorDashboard.html';
            }
        } else {
            alert("Invalid doctor credentials!");
        }
    } catch (error) {
        console.error("Doctor Login Error:", error);
        alert("An error occurred while trying to log in. Please try again later.");
    }
};

/**
 * Handle Patient Login Submission
 */
window.patientLoginHandler = async function () {
    const emailInput = document.getElementById('patientEmail');
    const passwordInput = document.getElementById('patientPassword');

    if (!emailInput || !passwordInput) return;

    const email = emailInput.value;
    const password = passwordInput.value;
    const patient = { email, password };

    try {
        const response = await fetch(PATIENT_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(patient)
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);

            if (typeof selectRole === 'function') {
                selectRole('patient');
            } else {
                localStorage.setItem('userRole', 'patient');
                window.location.href = '/pages/patientDashboard.html';
            }
        } else {
            alert("Invalid patient credentials!");
        }
    } catch (error) {
        console.error("Patient Login Error:", error);
        alert("An error occurred while trying to log in. Please try again later.");
    }
};
