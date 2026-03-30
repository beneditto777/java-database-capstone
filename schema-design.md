# Smart Clinic Data Schema Design

## MySQL Database Design
*MySQL is used here for structured, core operational data where relationships (like a patient having an appointment with a specific doctor) need to be strictly enforced using Foreign Keys and ACID compliance.*

### Table: admins
- `id`: BIGINT, Primary Key, Auto Increment
- `username`: VARCHAR(50), Not Null, Unique
- `password_hash`: VARCHAR(255), Not Null
- `email`: VARCHAR(100), Not Null, Unique
- `created_at`: DATETIME, Default Current_Timestamp

### Table: doctors
- `id`: BIGINT, Primary Key, Auto Increment
- `first_name`: VARCHAR(50), Not Null
- `last_name`: VARCHAR(50), Not Null
- `specialty`: VARCHAR(100), Not Null
- `email`: VARCHAR(100), Not Null, Unique
- `phone`: VARCHAR(20)
- `is_active`: BOOLEAN, Default True *(Used for "soft deletes" so historical appointments don't break if a doctor leaves)*

### Table: patients
- `id`: BIGINT, Primary Key, Auto Increment
- `first_name`: VARCHAR(50), Not Null
- `last_name`: VARCHAR(50), Not Null
- `date_of_birth`: DATE, Not Null
- `email`: VARCHAR(100), Not Null, Unique
- `phone_number`: VARCHAR(20)
- `registered_at`: DATETIME, Default Current_Timestamp

### Table: appointments
- `id`: BIGINT, Primary Key, Auto Increment
- `doctor_id`: BIGINT, Not Null, Foreign Key → doctors(id)
- `patient_id`: BIGINT, Not Null, Foreign Key → patients(id)
- `appointment_time`: DATETIME, Not Null
- `duration_minutes`: INT, Default 60
- `status`: ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'), Default 'SCHEDULED'
- `notes`: TEXT *(Brief reason for visit provided by patient during booking)*

---

## MongoDB Collection Design
*MongoDB is used here for data that is unstructured, variable in length, or heavily nested. A prescription is a great use case because the number of medications, instructions, and pharmacy details can vary wildly from patient to patient, making a rigid SQL table cumbersome.*

### Collection: prescriptions
This collection stores the medical outcomes of an appointment. It links back to the SQL database using `appointmentId` and `patientId`, but stores the complex, free-form medical data locally in the document.

```json
{
  "_id": "ObjectId('65f1a2b3c4d5e6f7a8b9c0d1')",
  "appointmentId": 142,
  "patientId": 89,
  "doctorName": "Dr. Sarah Jenkins",
  "issueDate": "2026-03-30T14:30:00Z",
  "diagnoses": [
    "Acute Bronchitis",
    "Mild Hypertension"
  ],
  "medications": [
    {
      "name": "Amoxicillin",
      "dosage": "500mg",
      "frequency": "Every 8 hours",
      "duration": "7 days",
      "refillsAllowed": 0
    },
    {
      "name": "Lisinopril",
      "dosage": "10mg",
      "frequency": "Once daily in the morning",
      "duration": "30 days",
      "refillsAllowed": 3
    }
  ],
  "doctorGeneralNotes": "Patient advised to rest and drink plenty of fluids. Follow up in 2 weeks if cough persists.",
  "preferredPharmacy": {
    "name": "Jakarta Central Pharmacy",
    "phone": "+62 21 555 0192",
    "address": "Jl. Sudirman No. 45, South Jakarta"
  }
}
