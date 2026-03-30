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