// patientDashboard.js
import { getDoctors, filterDoctors } from './services/doctorServices.js';
import { openModal } from './components/modals.js';
import { createDoctorCard } from './components/doctorCard.js';
import { patientSignup, patientLogin } from './services/patientServices.js';

document.addEventListener("DOMContentLoaded", () => {
    loadDoctorCards();

    // Bind Modals
    const signupBtn = document.getElementById("patientSignup");
    if (signupBtn) {
        signupBtn.addEventListener("click", () => openModal("patientSignup"));
    }

    const loginBtn = document.getElementById("patientLogin");
    if (loginBtn) {
        loginBtn.addEventListener("click", () => openModal("patientLogin"));
    }

    // Bind Search/Filters
    document.getElementById("searchBar").addEventListener("input", filterDoctorsOnChange);
    document.getElementById("filterTime").addEventListener("change", filterDoctorsOnChange);
    document.getElementById("filterSpecialty").addEventListener("change", filterDoctorsOnChange);
});

function loadDoctorCards() {
    getDoctors()
        .then(doctors => {
            const contentDiv = document.getElementById("content");
            contentDiv.innerHTML = "";

            doctors.forEach(doctor => {
                const card = createDoctorCard(doctor);
                contentDiv.appendChild(card);
            });
        })
        .catch(error => {
            console.error("Failed to load doctors:", error);
        });
}

function filterDoctorsOnChange() {
    const searchBar = document.getElementById("searchBar").value.trim();
    const filterTime = document.getElementById("filterTime").value;
    const filterSpecialty = document.getElementById("filterSpecialty").value;

    const name = searchBar.length > 0 ? searchBar : null;
    const time = filterTime.length > 0 ? filterTime : null;
    const specialty = filterSpecialty.length > 0 ? filterSpecialty : null;

    filterDoctors(name, time, specialty)
        .then(response => {
            const doctors = response.doctors ? response.doctors : response;
            const contentDiv = document.getElementById("content");
            contentDiv.innerHTML = "";

            if (doctors && doctors.length > 0) {
                doctors.forEach(doctor => {
                    const card = createDoctorCard(doctor);
                    contentDiv.appendChild(card);
                });
            } else {
                contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
            }
        })
        .catch(error => {
            console.error("Failed to filter doctors:", error);
            alert("An error occurred while filtering doctors.");
        });
}

window.signupPatient = async function () {
    try {
        const name = document.getElementById("name").value;
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;
        const phone = document.getElementById("phone").value;
        const address = document.getElementById("address").value;

        const data = { name, email, password, phone, address };
        const { success, message } = await patientSignup(data);

        if (success) {
            alert(message);
            document.getElementById("modal").style.display = "none";
            window.location.reload();
        } else {
            alert(message);
        }
    } catch (error) {
        console.error("Signup failed:", error);
        alert("An error occurred while signing up.");
    }
};

window.loginPatient = async function () {
    try {
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;

        const data = { email, password };
        const response = await patientLogin(data);

        if (response.ok) {
            const result = await response.json();

            // Set session variables
            localStorage.setItem('token', result.token);
            if (typeof selectRole === 'function') {
                selectRole('loggedPatient');
            } else {
                localStorage.setItem('userRole', 'loggedPatient');
            }

            window.location.href = '/pages/patientDashboard.html';
        } else {
            alert('Invalid credentials!');
        }
    } catch (error) {
        console.error("Failed to Login : ", error);
        alert("Failed to connect to the server.");
    }
};
