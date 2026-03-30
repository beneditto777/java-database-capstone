# User Story Template

**Title:**
_As an admin, I want to log into the portal with my username and password, so that I can manage the platform securely._
**Acceptance Criteria:**
1. The login page requires a valid username and password.
2. Entering correct credentials successfully redirects the user to the admin dashboard.
3. Entering incorrect credentials displays a clear error message and prevents access.
**Priority:** High
**Story Points:** 3
**Notes:**
- Ensure session tokens are generated securely upon successful login.

---

**Title:**
_As an admin, I want to log out of the portal, so that I can protect system access when I am no longer actively managing the platform._
**Acceptance Criteria:**
1. A "Logout" button is clearly visible and accessible from the admin dashboard.
2. Clicking the logout button terminates the active user session and redirects to the login page.
3. Attempting to use the browser's "Back" button after logging out prevents access to authenticated pages.
**Priority:** High
**Story Points:** 2
**Notes:**
- All session data and cookies should be cleared upon logout.

---

**Title:**
_As an admin, I want to add doctors to the portal, so that they can be registered in the system and begin taking patient appointments._
**Acceptance Criteria:**
1. The admin dashboard contains a form to input new doctor details (e.g., Name, Specialty, Contact Info).
2. Submitting the form with valid information creates a new doctor record in the database.
3. A success message is displayed once the doctor is added.
**Priority:** High
**Story Points:** 5
**Notes:**
- Form fields must include validation (e.g., ensuring email addresses are formatted correctly).

---

**Title:**
_As an admin, I want to delete a doctor's profile from the portal, so that the system only reflects currently active staff members._
**Acceptance Criteria:**
1. The admin can view a list of all current doctors with a "Delete" option next to each profile.
2. Clicking "Delete" triggers a confirmation prompt to prevent accidental deletions.
3. Confirming the deletion removes the doctor from the active portal view.
**Priority:** Medium
**Story Points:** 3
**Notes:**
- Consider a "soft delete" (changing status to inactive) rather than a hard delete to preserve historical appointment records associated with that doctor.

---

**Title:**
_As an admin, I want to run a stored procedure in the MySQL CLI to get the number of appointments per month, so that I can track usage statistics and platform growth._
**Acceptance Criteria:**
1. A stored procedure is created and exists within the MySQL database to calculate monthly appointment totals.
2. The admin can successfully execute this stored procedure directly from the MySQL CLI.
3. 3. The query output accurately displays the total count of appointments grouped by month and year.
**Priority:** Low
**Story Points:** 5
**Notes:**
- The database user account provided to the admin for CLI access must have the correct permissions to `EXECUTE` stored procedures.

---

**Title:**
_As a patient, I want to view a list of doctors without logging in, so that I can explore options before registering._
**Acceptance Criteria:**
1. Unauthenticated users can successfully navigate to the public "Doctors" page.
2. A list of doctors, including their names and specialties, is visible on the page.
3. Clicking a "Book Appointment" button prompts the unauthenticated user to log in or sign up.
**Priority:** Medium
**Story Points:** 2
**Notes:**
- Ensure the backend REST API endpoint for fetching the doctor list is public and does not require an authentication token.

---

**Title:**
_As a patient, I want to sign up using my email and password, so that I can book appointments._
**Acceptance Criteria:**
1. The registration page contains a form requiring an email, password, and basic personal details.
2. The form validates that the email is in the correct format and the password meets security constraints.
3. Upon successful submission, the system creates a patient record and redirects the user to the login page with a success message.
**Priority:** High
**Story Points:** 3
**Notes:**
- Ensure duplicate email registrations are prevented and return a user-friendly error message.

---

**Title:**
_As a patient, I want to log into the portal, so that I can manage my bookings._
**Acceptance Criteria:**
1. The login form requires a valid registered email and password.
2. Entering correct credentials authenticates the user and redirects them to the Patient Dashboard.
3. Entering incorrect credentials displays a clear "Invalid email or password" error message.
**Priority:** High
**Story Points:** 2
**Notes:**
- Upon successful login, securely store the JWT/session token in the browser's local storage for subsequent API requests.

---

**Title:**
_As a patient, I want to log out of the portal, so that I can secure my account._
**Acceptance Criteria:**
1. A "Logout" button is clearly visible in the navigation menu when logged in.
2. Clicking the logout button clears the user's session data and authentication tokens from the browser.
3. The user is instantly redirected to the public homepage or login screen.
**Priority:** Medium
**Story Points:** 1
**Notes:**
- Attempting to press the browser "Back" button after logging out should not grant access to the protected dashboard.

---

**Title:**
_As a patient, I want to log in and book an hour-long appointment, so that I can consult with a doctor._
**Acceptance Criteria:**
1. The logged-in patient can select a specific doctor and view their available calendar dates and time slots.
2. The system allows the user to select an open 1-hour time slot and confirm the booking.
3. Upon confirmation, the time slot is marked as unavailable for other users, and the appointment is saved to the database.
**Priority:** High
**Story Points:** 5
**Notes:**
- The backend Service layer must include validation to prevent double-booking the same time slot if two patients try to book simultaneously.

---

**Title:**
_As a patient, I want to view my upcoming appointments, so that I can prepare accordingly._
**Acceptance Criteria:**
1. The Patient Dashboard displays a clear list or table of all future appointments tied to the user's account.
2. Each appointment entry displays the date, time, and the assigned doctor's name.
3. The list is sorted chronologically, with the most imminent appointment appearing first.
**Priority:** High
**Story Points:** 3
**Notes:**
- Past appointments should be filtered out of this view, potentially moved to a separate "Appointment History" tab.

---

**Title:**
_As a doctor, I want to log into the portal, so that I can manage my appointments._
**Acceptance Criteria:**
1. The login form requires a registered email and password.
2. Entering valid credentials authenticates the user as a Doctor and redirects them to the Doctor Dashboard.
3. Entering invalid credentials displays an "Invalid email or password" error message.
**Priority:** High
**Story Points:** 3
**Notes:**
- The backend must verify that the authenticated user possesses the 'Doctor' role before granting dashboard access.

---

**Title:**
_As a doctor, I want to log out of the portal, so that I can protect my data._
**Acceptance Criteria:**
1. A "Logout" button is visible and accessible from the Doctor Dashboard navigation.
2. Clicking the button securely destroys the active session and clears local storage tokens.
3. The user is redirected back to the main login screen.
**Priority:** High
**Story Points:** 2
**Notes:**
- Using the browser's "Back" button after logging out must not allow access to protected patient data.

---

**Title:**
_As a doctor, I want to view my appointment calendar, so that I can stay organized._
**Acceptance Criteria:**
1. The dashboard displays a clear calendar or list view of all upcoming appointments assigned to the doctor.
2. Each appointment entry shows the patient's name, the scheduled time, and the appointment status.
3. The calendar allows the doctor to filter or toggle between daily and weekly views.
**Priority:** High
**Story Points:** 5
**Notes:**
- Ensure the front-end calendar dynamically fetches data from the backend REST API based on the selected date range.

---

**Title:**
_As a doctor, I want to mark my unavailability, so that I can inform patients of only the available slots._
**Acceptance Criteria:**
1. The dashboard includes a scheduling tool allowing the doctor to select specific dates and time slots to block off.
2. Saving the blocked times updates the backend database successfully.
3. Once marked unavailable, these time slots immediately disappear from the patient-facing booking screen.
**Priority:** High
**Story Points:** 5
**Notes:**
- Needs validation to ensure a doctor cannot mark a slot as unavailable if a patient has already booked it (without triggering a cancellation workflow).

---

**Title:**
_As a doctor, I want to update my profile with specialization and contact information, so that patients have up-to-date information._
**Acceptance Criteria:**
1. The portal provides a "Profile Settings" page with a form to edit the doctor's name, specialty, and contact details.
2. The form validates that required fields (like specialty) are not left blank.
3. Upon saving, the updated information is instantly reflected on the public "Available Doctors" list.
**Priority:** Medium
**Story Points:** 3
**Notes:**
- Ensure database updates properly sanitize inputs to prevent SQL injection or cross-site scripting (XSS).

---

**Title:**
_As a doctor, I want to view the patient details for upcoming appointments, so that I can be prepared._
**Acceptance Criteria:**
1. Clicking on a specific appointment from the calendar opens a detailed view of the patient.
2. The detailed view displays the patient's age, contact information, and any notes provided during the booking process.
3. The data is retrieved securely, ensuring patient privacy is maintained.
**Priority:** High
**Story Points:** 3
**Notes:**
- Ensure only the assigned doctor can view these specific patient details to comply with privacy standards.
