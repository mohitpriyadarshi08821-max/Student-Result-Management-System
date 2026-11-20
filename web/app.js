/**
 * Student Result Management System - Web Application Engine
 * Native Web & Mobile Implementation with LocalStorage & Live JSON Sync
 */

// Initial Authoritative Seed Data (Exactly matching srms_data.json & Desktop App)
const SEED_DATA = {
  students: [
    {"studentId":1,"rollNumber":"2510001283","firstName":"Utkarsh","lastName":"Mishra","email":"utkarsh.mishra@example.com","course":"B.Tech Computer Science","semester":4,"gender":"Male","dob":"2007-11-20","age":19,"grade":"A-"},
    {"studentId":2,"rollNumber":"2510001205","firstName":"Abhishek","lastName":"Kumar","email":"abhishek.kumar@example.com","course":"B.Tech Information Technology","semester":4,"gender":"Male","dob":"2007-11-20","age":19,"grade":"B"},
    {"studentId":3,"rollNumber":"2510001348","firstName":"Aditya","lastName":"Saini","email":"aditya.saini@example.com","course":"BCA","semester":2,"gender":"Male","dob":"2008-11-20","age":18,"grade":"A"},
    {"studentId":4,"rollNumber":"2510001320","firstName":"Mohit","lastName":"Priyadarshi","email":"mohit.priyadarshi@example.com","course":"B.Tech Computer Science","semester":3,"gender":"Male","dob":"2007-11-20","age":19,"grade":"B"},
    {"studentId":5,"rollNumber":"2510001331","firstName":"Moulik","lastName":"Tiwari","email":"moulik.tiwari@example.com","course":"BCA","semester":2,"gender":"Male","dob":"2007-11-20","age":19,"grade":"A-"},
    {"studentId":6,"rollNumber":"2510001319","firstName":"Adarsh","lastName":"Kumar","email":"adarsh.kumar@example.com","course":"B.Tech IT","semester":4,"gender":"Male","dob":"2006-11-20","age":20,"grade":"A"},
    {"studentId":7,"rollNumber":"2510001212","firstName":"Akshay","lastName":"Bharadwaj","email":"akshay.bharadwaj@example.com","course":"MCA","semester":2,"gender":"Male","dob":"2007-11-20","age":19,"grade":"B"}
  ],
  subjects: [
    {"subjectId":1,"subjectCode":"CS107","subjectName":"Computer Architecture And Organization","maxMarks":100,"passMarks":35,"credits":4},
    {"subjectId":2,"subjectCode":"CS102","subjectName":"Database Management Systems","maxMarks":100,"passMarks":35,"credits":4},
    {"subjectId":3,"subjectCode":"CS103","subjectName":"Data Structures & Algorithms","maxMarks":100,"passMarks":35,"credits":4},
    {"subjectId":4,"subjectCode":"CS104","subjectName":"Computer Networks","maxMarks":100,"passMarks":35,"credits":4},
    {"subjectId":5,"subjectCode":"CS105","subjectName":"Operating Systems","maxMarks":100,"passMarks":35,"credits":4},
    {"subjectId":6,"subjectCode":"CS106","subjectName":"Artificial Intelligence","maxMarks":100,"passMarks":35,"credits":4}
  ],
  results: [
    {"resultId":2,"studentId":1,"subjectId":2,"marksObtained":84.0,"grade":"A-"},
    {"resultId":3,"studentId":1,"subjectId":3,"marksObtained":85.0,"grade":"A-"},
    {"resultId":5,"studentId":2,"subjectId":2,"marksObtained":76.0,"grade":"B"},
    {"resultId":7,"studentId":3,"subjectId":2,"marksObtained":90.0,"grade":"A"},
    {"resultId":9,"studentId":5,"subjectId":2,"marksObtained":86.0,"grade":"A-"},
    {"resultId":10,"studentId":6,"subjectId":2,"marksObtained":87.0,"grade":"A"},
    {"resultId":11,"studentId":6,"subjectId":3,"marksObtained":69.0,"grade":"C"},
    {"resultId":12,"studentId":6,"subjectId":4,"marksObtained":80.0,"grade":"A"},
    {"resultId":13,"studentId":6,"subjectId":5,"marksObtained":66.0,"grade":"C"},
    {"resultId":14,"studentId":6,"subjectId":1,"marksObtained":88.0,"grade":"A"},
    {"resultId":17,"studentId":4,"subjectId":1,"marksObtained":90.0,"grade":"A+"},
    {"resultId":18,"studentId":4,"subjectId":2,"marksObtained":90.0,"grade":"A+"},
    {"resultId":19,"studentId":4,"subjectId":3,"marksObtained":88.0,"grade":"A"},
    {"resultId":20,"studentId":4,"subjectId":4,"marksObtained":91.0,"grade":"A+"},
    {"resultId":21,"studentId":4,"subjectId":5,"marksObtained":81.0,"grade":"A"},
    {"resultId":22,"studentId":4,"subjectId":6,"marksObtained":93.0,"grade":"A+"},
    {"resultId":23,"studentId":6,"subjectId":6,"marksObtained":88.0,"grade":"A"},
    {"resultId":24,"studentId":3,"subjectId":1,"marksObtained":77.0,"grade":"B"},
    {"resultId":25,"studentId":3,"subjectId":3,"marksObtained":66.0,"grade":"C"},
    {"resultId":26,"studentId":3,"subjectId":4,"marksObtained":79.0,"grade":"B"},
    {"resultId":27,"studentId":3,"subjectId":5,"marksObtained":34.0,"grade":"F"},
    {"resultId":28,"studentId":3,"subjectId":6,"marksObtained":80.0,"grade":"A"},
    {"resultId":29,"studentId":2,"subjectId":1,"marksObtained":88.0,"grade":"A"},
    {"resultId":30,"studentId":2,"subjectId":3,"marksObtained":79.0,"grade":"B"},
    {"resultId":31,"studentId":2,"subjectId":4,"marksObtained":77.0,"grade":"B"},
    {"resultId":32,"studentId":2,"subjectId":5,"marksObtained":84.0,"grade":"A"},
    {"resultId":33,"studentId":2,"subjectId":6,"marksObtained":81.0,"grade":"A"},
    {"resultId":34,"studentId":7,"subjectId":1,"marksObtained":76.0,"grade":"B"},
    {"resultId":35,"studentId":7,"subjectId":2,"marksObtained":80.0,"grade":"A"},
    {"resultId":36,"studentId":7,"subjectId":3,"marksObtained":79.0,"grade":"B"},
    {"resultId":37,"studentId":7,"subjectId":4,"marksObtained":83.0,"grade":"A"},
    {"resultId":38,"studentId":7,"subjectId":5,"marksObtained":90.0,"grade":"A+"},
    {"resultId":39,"studentId":7,"subjectId":6,"marksObtained":83.0,"grade":"A"},
    {"resultId":40,"studentId":5,"subjectId":1,"marksObtained":80.0,"grade":"A"},
    {"resultId":41,"studentId":5,"subjectId":3,"marksObtained":77.0,"grade":"B"},
    {"resultId":42,"studentId":5,"subjectId":4,"marksObtained":85.0,"grade":"A"},
    {"resultId":43,"studentId":5,"subjectId":5,"marksObtained":65.0,"grade":"C"},
    {"resultId":44,"studentId":5,"subjectId":6,"marksObtained":72.0,"grade":"B"},
    {"resultId":45,"studentId":1,"subjectId":1,"marksObtained":94.0,"grade":"A+"},
    {"resultId":46,"studentId":1,"subjectId":4,"marksObtained":79.0,"grade":"B"},
    {"resultId":47,"studentId":1,"subjectId":5,"marksObtained":60.0,"grade":"C"},
    {"resultId":125,"studentId":1,"subjectId":6,"marksObtained":75.0,"grade":"B"}
  ]
};

// Application State
let appData = {
  students: [],
  subjects: [],
  results: [],
  stats: {
    students: { total: 7, added: 6, updated: 2, deleted: 1 },
    subjects: { total: 6, added: 6, updated: 0, deleted: 0 }
  }
};

let currentVerifiedStudent = null;
let currentMarksheetSummary = null;

// Initialize Data Store
function initDataStore() {
  const stored = localStorage.getItem('srms_data_store');
  if (stored) {
    try {
      const parsed = JSON.parse(stored);
      if (parsed.students && parsed.students.length >= 7) {
        appData = parsed;
      } else {
        resetToSeedData();
      }
    } catch (e) {
      resetToSeedData();
    }
  } else {
    resetToSeedData();
  }
  
  // Ensure default stats match exact user requirements: Total 7, Added 6, Updated 2, Deleted 1
  if (!appData.stats || !appData.stats.students) {
    appData.stats = {
      students: { total: 7, added: 6, updated: 2, deleted: 1 },
      subjects: { total: 6, added: 6, updated: 0, deleted: 0 }
    };
  }
  saveDataStore();
}

function resetToSeedData() {
  appData = JSON.parse(JSON.stringify(SEED_DATA));
  appData.stats = {
    students: { total: 7, added: 6, updated: 2, deleted: 1 },
    subjects: { total: 6, added: 6, updated: 0, deleted: 0 }
  };
  saveDataStore();
}

function saveDataStore() {
  localStorage.setItem('srms_data_store', JSON.stringify(appData));
  // Try sending to local server if running
  if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    fetch('/api/sync', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        students: appData.students,
        subjects: appData.subjects,
        results: appData.results
      })
    }).catch(() => {});
  }
}

// Grading & SGPA Calculation Engine
function calculateGrade(marks, passMarks = 35) {
  if (marks < passMarks) return { grade: 'F', points: 0.0, status: 'FAIL' };
  if (marks >= 90) return { grade: 'A+', points: 10.0, status: 'PASS' };
  if (marks >= 80) return { grade: 'A', points: 9.0, status: 'PASS' };
  if (marks >= 75) return { grade: 'A-', points: 8.5, status: 'PASS' };
  if (marks >= 70) return { grade: 'B', points: 8.0, status: 'PASS' };
  if (marks >= 60) return { grade: 'C', points: 6.5, status: 'PASS' };
  if (marks >= 50) return { grade: 'D', points: 5.0, status: 'PASS' };
  return { grade: 'E', points: 4.0, status: 'PASS' };
}

function getGradeBadgeClass(grade) {
  if (!grade) return 'c';
  const g = grade.trim().toLowerCase();
  if (g === 'a+') return 'a-plus';
  if (g === 'a') return 'a';
  if (g === 'a-') return 'a-minus';
  if (g === 'b') return 'b';
  if (g === 'c') return 'c';
  if (g === 'f') return 'f';
  return 'b';
}

// Compute Summary For a Student
function computeStudentSummary(studentId) {
  const student = appData.students.find(s => s.studentId === studentId);
  if (!student) return null;

  const studentResults = appData.results.filter(r => r.studentId === studentId);
  const rows = [];
  let totalMarks = 0;
  let maxMarks = 0;
  let totalWeightedPoints = 0;
  let totalCredits = 0;
  let hasFail = false;

  studentResults.forEach(r => {
    const subject = appData.subjects.find(sub => sub.subjectId === r.subjectId);
    if (!subject) return;

    const evaluation = calculateGrade(r.marksObtained, subject.passMarks);
    const credits = subject.credits || 4;
    totalMarks += r.marksObtained;
    maxMarks += subject.maxMarks;
    totalCredits += credits;
    totalWeightedPoints += (evaluation.points * credits);
    if (evaluation.status === 'FAIL') hasFail = true;

    rows.push({
      resultId: r.resultId,
      subjectId: subject.subjectId,
      subjectCode: subject.subjectCode,
      subjectName: subject.subjectName,
      credits: credits,
      maxMarks: subject.maxMarks,
      passMarks: subject.passMarks,
      marksObtained: r.marksObtained,
      grade: evaluation.grade,
      status: evaluation.status
    });
  });

  const percentage = maxMarks > 0 ? (totalMarks / maxMarks) * 100 : 0;
  const sgpa = totalCredits > 0 ? (totalWeightedPoints / totalCredits) : 0;
  let overallGrade = 'B';
  if (hasFail) {
    overallGrade = 'F';
  } else if (percentage >= 90) {
    overallGrade = 'A+';
  } else if (percentage >= 80) {
    overallGrade = 'A';
  } else if (percentage >= 75) {
    overallGrade = 'A-';
  } else if (percentage >= 70) {
    overallGrade = 'B';
  } else if (percentage >= 60) {
    overallGrade = 'C';
  } else {
    overallGrade = 'D';
  }

  let division = 'Pass Class';
  if (!hasFail) {
    if (percentage >= 75) division = 'First Class with Distinction';
    else if (percentage >= 60) division = 'First Class';
    else if (percentage >= 50) division = 'Second Class';
  } else {
    division = 'Fail / Backlog';
  }

  return {
    student,
    rows,
    totalSubjects: rows.length,
    totalMarks,
    maxMarks,
    percentage: percentage.toFixed(2),
    sgpa: sgpa.toFixed(2),
    overallGrade,
    division,
    status: hasFail || rows.length === 0 ? 'FAIL' : 'PASS',
    date: new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })
  };
}

// DOM Rendering Functions

// Render Students Table & Stat Cards
function renderStudents(filter = '') {
  const tbody = document.getElementById('students-table-body');
  if (!tbody) return;

  const query = filter.toLowerCase().trim();
  const filtered = appData.students.filter(s => {
    const fullName = `${s.firstName} ${s.lastName}`.toLowerCase();
    const roll = s.rollNumber.toLowerCase();
    const course = (s.course || '').toLowerCase();
    return fullName.includes(query) || roll.includes(query) || course.includes(query);
  });

  tbody.innerHTML = '';
  filtered.forEach(s => {
    const tr = document.createElement('tr');
    const badgeClass = getGradeBadgeClass(s.grade || 'B');
    tr.innerHTML = `
      <td><span class="roll-text">${s.rollNumber}</span></td>
      <td>
        <div class="name-cell">${s.firstName} ${s.lastName}</div>
        <div class="sub-meta">${s.course || 'B.Tech'} • Sem ${s.semester || 1}</div>
      </td>
      <td>${s.age || 19}</td>
      <td><span class="grade-badge ${badgeClass}">${s.grade || 'B'}</span></td>
      <td>
        <div style="display:flex; gap:6px;">
          <button class="btn btn-secondary btn-sm" onclick="viewStudentDetails(${s.studentId})" title="View Details">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            View
          </button>
          <button class="btn btn-secondary btn-sm" onclick="openUpdateStudentModal(${s.studentId})" title="Edit Student">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            Edit
          </button>
          <button class="btn btn-secondary btn-sm" style="color:var(--danger);" onclick="deleteStudent(${s.studentId})" title="Delete">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          </button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });

  // Update Stat Cards (Total Students 7, Added 6, Updated 2, Deleted 1)
  const totalCountEl = document.getElementById('stat-total-students');
  const addedCountEl = document.getElementById('stat-added-students');
  const updatedCountEl = document.getElementById('stat-updated-students');
  const deletedCountEl = document.getElementById('stat-deleted-students');

  if (totalCountEl) totalCountEl.innerText = appData.stats.students.total;
  if (addedCountEl) addedCountEl.innerText = appData.stats.students.added;
  if (updatedCountEl) updatedCountEl.innerText = appData.stats.students.updated;
  if (deletedCountEl) deletedCountEl.innerText = appData.stats.students.deleted;
}

// Render Subjects Table & Stat Cards
function renderSubjects(filter = '') {
  const tbody = document.getElementById('subjects-table-body');
  if (!tbody) return;

  const query = filter.toLowerCase().trim();
  const filtered = appData.subjects.filter(sub => {
    return sub.subjectCode.toLowerCase().includes(query) || sub.subjectName.toLowerCase().includes(query);
  });

  tbody.innerHTML = '';
  filtered.forEach(sub => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><span class="roll-text">${sub.subjectCode}</span></td>
      <td><strong>${sub.subjectName}</strong></td>
      <td>${sub.maxMarks}</td>
      <td>${sub.passMarks}</td>
      <td><span class="grade-badge a">${sub.credits || 4}</span></td>
      <td>
        <div style="display:flex; gap:6px;">
          <button class="btn btn-secondary btn-sm" onclick="openUpdateSubjectModal(${sub.subjectId})">
            Edit
          </button>
          <button class="btn btn-secondary btn-sm" style="color:var(--danger);" onclick="deleteSubject(${sub.subjectId})">
            Delete
          </button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });

  const totalSubEl = document.getElementById('stat-total-subjects');
  const addedSubEl = document.getElementById('stat-added-subjects');
  const updatedSubEl = document.getElementById('stat-updated-subjects');
  const deletedSubEl = document.getElementById('stat-deleted-subjects');

  if (totalSubEl) totalSubEl.innerText = appData.subjects.length;
  if (addedSubEl) addedSubEl.innerText = appData.stats.subjects.added;
  if (updatedSubEl) updatedSubEl.innerText = appData.stats.subjects.updated;
  if (deletedSubEl) deletedSubEl.innerText = appData.stats.subjects.deleted;
}

// Render Result Entry Panel
function setupResultEntry() {
  const subjectSelect = document.getElementById('entry-subject-select');
  if (subjectSelect) {
    subjectSelect.innerHTML = '<option value="">-- Select Curriculum Subject --</option>';
    appData.subjects.forEach(sub => {
      const opt = document.createElement('option');
      opt.value = sub.subjectId;
      opt.innerText = `${sub.subjectCode} - ${sub.subjectName} (${sub.credits || 4} Credits)`;
      subjectSelect.appendChild(opt);
    });
  }

  // Update Global Result Entry Stats
  updateResultEntryStats();
}

function updateResultEntryStats() {
  let totalRec = appData.results.length;
  let passedRec = 0;
  let failedRec = 0;
  let totalPctSum = 0;

  appData.results.forEach(r => {
    const sub = appData.subjects.find(s => s.subjectId === r.subjectId);
    const pass = sub ? sub.passMarks : 35;
    if (r.marksObtained >= pass) passedRec++;
    else failedRec++;
    totalPctSum += r.marksObtained;
  });

  const avg = totalRec > 0 ? (totalPctSum / totalRec).toFixed(1) + '%' : '0%';

  const elTotal = document.getElementById('stat-recorded-results');
  const elPass = document.getElementById('stat-passed-results');
  const elFail = document.getElementById('stat-failed-results');
  const elAvg = document.getElementById('stat-avg-score');

  if (elTotal) elTotal.innerText = totalRec;
  if (elPass) elPass.innerText = passedRec;
  if (elFail) elFail.innerText = failedRec;
  if (elAvg) elAvg.innerText = avg;
}

function verifyStudentForEntry(rollNumber) {
  const roll = (rollNumber || document.getElementById('entry-roll-input').value).trim();
  const student = appData.students.find(s => s.rollNumber.toLowerCase() === roll.toLowerCase());
  const banner = document.getElementById('entry-verified-banner');
  const tbody = document.getElementById('entry-results-table-body');

  if (!student) {
    if (banner) {
      banner.style.display = 'block';
      banner.className = 'verified-pill';
      banner.style.background = 'var(--danger-bg)';
      banner.style.color = 'var(--danger)';
      banner.style.borderColor = 'var(--danger-border)';
      banner.innerText = 'Student not found. Please verify roll number.';
    }
    if (tbody) tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px; color:var(--text-muted);">No student selected. Enter roll number above.</td></tr>';
    currentVerifiedStudent = null;
    return;
  }

  currentVerifiedStudent = student;
  if (banner) {
    banner.style.display = 'inline-flex';
    banner.className = 'verified-pill';
    banner.style.background = 'var(--success-bg)';
    banner.style.color = 'var(--success)';
    banner.style.borderColor = 'var(--success-border)';
    banner.innerHTML = `✓ Verified: <strong>${student.firstName} ${student.lastName}</strong> | ${student.course || 'B.Tech'} (Sem ${student.semester || 1})`;
  }

  renderStudentResultsTable(student.studentId);
}

function renderStudentResultsTable(studentId) {
  const tbody = document.getElementById('entry-results-table-body');
  if (!tbody) return;

  const results = appData.results.filter(r => r.studentId === studentId);
  if (results.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:20px; color:var(--text-muted);">No recorded exam marks for this student yet.</td></tr>';
    return;
  }

  tbody.innerHTML = '';
  results.forEach(r => {
    const sub = appData.subjects.find(s => s.subjectId === r.subjectId);
    if (!sub) return;
    const evalData = calculateGrade(r.marksObtained, sub.passMarks);
    const badgeClass = getGradeBadgeClass(evalData.grade);

    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${r.resultId}</td>
      <td><span class="roll-text">${sub.subjectCode}</span></td>
      <td><strong>${sub.subjectName}</strong></td>
      <td>${sub.maxMarks}</td>
      <td><strong>${r.marksObtained.toFixed(1)}</strong></td>
      <td><span class="grade-badge ${badgeClass}">${evalData.grade}</span></td>
      <td><span class="status-badge ${evalData.status.toLowerCase()}">${evalData.status}</span></td>
      <td>
        <button class="btn btn-secondary btn-sm" style="color:var(--danger);" onclick="deleteResultRecord(${r.resultId})">
          Remove
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function saveMarksEntry() {
  if (!currentVerifiedStudent) {
    alert('Please verify student roll number first.');
    return;
  }

  const subSelect = document.getElementById('entry-subject-select');
  const marksInput = document.getElementById('entry-marks-input');
  const subjectId = parseInt(subSelect.value, 10);
  const marks = parseFloat(marksInput.value);

  if (!subjectId) {
    alert('Please select a subject.');
    return;
  }
  if (isNaN(marks) || marks < 0 || marks > 100) {
    alert('Please enter valid marks between 0 and 100.');
    return;
  }

  const sub = appData.subjects.find(s => s.subjectId === subjectId);
  const evaluation = calculateGrade(marks, sub ? sub.passMarks : 35);

  // Check if result already exists for this student & subject
  const existingIdx = appData.results.findIndex(r => r.studentId === currentVerifiedStudent.studentId && r.subjectId === subjectId);
  if (existingIdx >= 0) {
    appData.results[existingIdx].marksObtained = marks;
    appData.results[existingIdx].grade = evaluation.grade;
  } else {
    const newId = appData.results.reduce((max, r) => Math.max(max, r.resultId), 0) + 1;
    appData.results.push({
      resultId: newId,
      studentId: currentVerifiedStudent.studentId,
      subjectId: subjectId,
      marksObtained: marks,
      grade: evaluation.grade
    });
  }

  saveDataStore();
  renderStudentResultsTable(currentVerifiedStudent.studentId);
  updateResultEntryStats();
  marksInput.value = '';
  subSelect.value = '';
  alert('Marks recorded successfully!');
}

function deleteResultRecord(resultId) {
  if (!confirm('Are you sure you want to delete this result entry?')) return;
  appData.results = appData.results.filter(r => r.resultId !== resultId);
  saveDataStore();
  if (currentVerifiedStudent) {
    renderStudentResultsTable(currentVerifiedStudent.studentId);
  }
  updateResultEntryStats();
}

// Render View Results & Marksheets Panel
function renderMarksheetView(rollNumber) {
  const roll = (rollNumber || document.getElementById('marksheet-roll-input').value).trim();
  const student = appData.students.find(s => s.rollNumber.toLowerCase() === roll.toLowerCase());

  const verifiedBanner = document.getElementById('marksheet-verified-banner');
  const tbody = document.getElementById('marksheet-table-body');

  if (!student) {
    if (verifiedBanner) {
      verifiedBanner.style.display = 'block';
      verifiedBanner.className = 'verified-pill';
      verifiedBanner.style.background = 'var(--danger-bg)';
      verifiedBanner.style.color = 'var(--danger)';
      verifiedBanner.style.borderColor = 'var(--danger-border)';
      verifiedBanner.innerText = 'Student record not found for roll: ' + roll;
    }
    if (tbody) tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; padding:24px; color:var(--text-muted);">No student marksheet to display.</td></tr>';
    currentMarksheetSummary = null;
    return;
  }

  const summary = computeStudentSummary(student.studentId);
  currentMarksheetSummary = summary;

  // Verified banner
  if (verifiedBanner) {
    verifiedBanner.style.display = 'inline-flex';
    verifiedBanner.className = 'verified-pill';
    verifiedBanner.style.background = 'var(--success-bg)';
    verifiedBanner.style.color = 'var(--success)';
    verifiedBanner.style.borderColor = 'var(--success-border)';
    verifiedBanner.innerHTML = `✓ Verified: <strong>${student.firstName} ${student.lastName}</strong> | ${student.course || 'B.Tech'} (Sem ${student.semester || 1})`;
  }

  // 4 Top Stat Cards on Marksheet Tab
  const cardSubj = document.getElementById('stat-marksheet-subjects');
  const cardMarks = document.getElementById('stat-marksheet-marks');
  const cardSgpa = document.getElementById('stat-marksheet-sgpa');
  const cardStatus = document.getElementById('stat-marksheet-status');

  if (cardSubj) cardSubj.innerText = summary.totalSubjects;
  if (cardMarks) cardMarks.innerText = `${summary.totalMarks.toFixed(1)} / ${summary.maxMarks}`;
  if (cardSgpa) cardSgpa.innerText = summary.sgpa;
  if (cardStatus) cardStatus.innerText = `${summary.status} (${summary.overallGrade})`;

  // Statement of Marks Table
  if (tbody) {
    tbody.innerHTML = '';
    summary.rows.forEach(r => {
      const badgeClass = getGradeBadgeClass(r.grade);
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><span class="roll-text">${r.subjectCode}</span></td>
        <td><strong>${r.subjectName}</strong></td>
        <td>${r.credits}</td>
        <td>${r.maxMarks}</td>
        <td>${r.passMarks}</td>
        <td><strong>${r.marksObtained.toFixed(1)}</strong></td>
        <td><span class="grade-badge ${badgeClass}">${r.grade}</span></td>
        <td><span class="status-badge ${r.status.toLowerCase()}">${r.status}</span></td>
      `;
      tbody.appendChild(tr);
    });
  }

  // Lower Summary Ribbon
  const ribbonMarks = document.getElementById('ribbon-total-marks');
  const ribbonPct = document.getElementById('ribbon-percentage');
  const ribbonSgpa = document.getElementById('ribbon-sgpa');
  const ribbonGrade = document.getElementById('ribbon-grade');
  const ribbonDate = document.getElementById('ribbon-date');
  const ribbonTag = document.getElementById('ribbon-status-tag');

  if (ribbonMarks) ribbonMarks.innerText = `${summary.totalMarks.toFixed(1)} / ${summary.maxMarks}`;
  if (ribbonPct) ribbonPct.innerText = `${summary.percentage}%`;
  if (ribbonSgpa) ribbonSgpa.innerText = summary.sgpa;
  if (ribbonGrade) ribbonGrade.innerText = `${summary.overallGrade} (${summary.division})`;
  if (ribbonDate) ribbonDate.innerText = summary.date;
  if (ribbonTag) {
    ribbonTag.className = `summary-status-tag ${summary.status.toLowerCase()}`;
    ribbonTag.innerText = `STATUS: ${summary.status}`;
  }

  // Update quick select active chips
  document.querySelectorAll('.quick-chip').forEach(chip => {
    chip.classList.toggle('active', chip.getAttribute('data-roll') === student.rollNumber);
  });
}

// Open Official Marksheet Print Modal
function openPrintMarksheetModal() {
  if (!currentMarksheetSummary) {
    alert('Please select or search for a student first.');
    return;
  }

  const s = currentMarksheetSummary;
  const container = document.getElementById('official-marksheet-container');
  if (!container) return;

  let tableRowsHtml = '';
  s.rows.forEach((r, idx) => {
    tableRowsHtml += `
      <tr>
        <td>${idx + 1}</td>
        <td>${r.subjectCode}</td>
        <td class="text-left">${r.subjectName}</td>
        <td>${r.credits}</td>
        <td>${r.maxMarks}</td>
        <td>${r.passMarks}</td>
        <td><strong>${r.marksObtained.toFixed(1)}</strong></td>
        <td><strong>${r.grade}</strong></td>
        <td>${r.status}</td>
      </tr>
    `;
  });

  container.innerHTML = `
    <div class="official-marksheet-doc">
      <div class="marksheet-header-block">
        <h2>STATE UNIVERSITY OF TECHNOLOGY & SCIENCE</h2>
        <h3>INSTITUTIONAL EXAMINATION & ACADEMIC EVALUATION DIVISION</h3>
        <p>Official Grade Report & Cumulative Statement of Academic Standing</p>
      </div>

      <div class="student-meta-grid">
        <div class="meta-row"><span class="meta-label">Student Name:</span> <span class="meta-val">${s.student.firstName} ${s.student.lastName}</span></div>
        <div class="meta-row"><span class="meta-label">Roll Number:</span> <span class="meta-val">${s.student.rollNumber}</span></div>
        <div class="meta-row"><span class="meta-label">Academic Program:</span> <span class="meta-val">${s.student.course || 'B.Tech'}</span></div>
        <div class="meta-row"><span class="meta-label">Semester / Term:</span> <span class="meta-val">Semester ${s.student.semester || 1}</span></div>
        <div class="meta-row"><span class="meta-label">Academic Session:</span> <span class="meta-val">2026-2027</span></div>
        <div class="meta-row"><span class="meta-label">Date of Evaluation:</span> <span class="meta-val">${s.date}</span></div>
      </div>

      <table class="doc-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Subject Code</th>
            <th>Course Title</th>
            <th>Credits</th>
            <th>Max Marks</th>
            <th>Min Marks</th>
            <th>Marks Obtained</th>
            <th>Grade</th>
            <th>Result</th>
          </tr>
        </thead>
        <tbody>
          ${tableRowsHtml}
        </tbody>
      </table>

      <div class="marksheet-aggregate-box">
        <div>
          <div class="agg-label">Total Marks</div>
          <div class="agg-val">${s.totalMarks.toFixed(1)} / ${s.maxMarks}</div>
        </div>
        <div>
          <div class="agg-label">Semester Percentage</div>
          <div class="agg-val">${s.percentage}%</div>
        </div>
        <div>
          <div class="agg-label">Semester SGPA</div>
          <div class="agg-val">${s.sgpa} / 10.0</div>
        </div>
        <div>
          <div class="agg-label">Final Evaluation</div>
          <div class="agg-val" style="color:${s.status === 'PASS' ? 'var(--success)' : 'var(--danger)'};">${s.status} (${s.overallGrade})</div>
        </div>
      </div>

      <div class="marksheet-signatures">
        <div class="sign-box">Prepared & Verified By</div>
        <div class="sign-box">Dean of Academics</div>
        <div class="sign-box">Controller of Examinations</div>
      </div>
    </div>
  `;

  document.getElementById('print-marksheet-modal').classList.add('active');
}

// Student Modal Management
function openAddStudentModal() {
  document.getElementById('student-modal-title').innerText = 'Add New Student';
  document.getElementById('student-form').reset();
  document.getElementById('student-id-field').value = '';
  document.getElementById('student-modal').classList.add('active');
}

function openUpdateStudentModal(studentId) {
  const s = appData.students.find(st => st.studentId === studentId);
  if (!s) return;

  document.getElementById('student-modal-title').innerText = 'Update Student Record';
  document.getElementById('student-id-field').value = s.studentId;
  document.getElementById('modal-roll-input').value = s.rollNumber;
  document.getElementById('modal-fname-input').value = s.firstName;
  document.getElementById('modal-lname-input').value = s.lastName;
  document.getElementById('modal-email-input').value = s.email || '';
  document.getElementById('modal-course-input').value = s.course || 'B.Tech Computer Science';
  document.getElementById('modal-semester-input').value = s.semester || 1;
  document.getElementById('modal-gender-input').value = s.gender || 'Male';
  document.getElementById('modal-grade-input').value = s.grade || 'B';
  document.getElementById('modal-age-input').value = s.age || 19;

  document.getElementById('student-modal').classList.add('active');
}

function saveStudentForm() {
  const idVal = document.getElementById('student-id-field').value;
  const roll = document.getElementById('modal-roll-input').value.trim();
  const fname = document.getElementById('modal-fname-input').value.trim();
  const lname = document.getElementById('modal-lname-input').value.trim();
  const email = document.getElementById('modal-email-input').value.trim();
  const course = document.getElementById('modal-course-input').value.trim();
  const sem = parseInt(document.getElementById('modal-semester-input').value, 10) || 1;
  const gender = document.getElementById('modal-gender-input').value;
  const grade = document.getElementById('modal-grade-input').value.trim() || 'B';
  const age = parseInt(document.getElementById('modal-age-input').value, 10) || 19;

  if (!roll || !fname || !lname) {
    alert('Please fill out Roll Number, First Name, and Last Name.');
    return;
  }

  if (idVal) {
    // Update existing student
    const studentId = parseInt(idVal, 10);
    const idx = appData.students.findIndex(s => s.studentId === studentId);
    if (idx >= 0) {
      appData.students[idx] = {
        ...appData.students[idx],
        rollNumber: roll,
        firstName: fname,
        lastName: lname,
        email,
        course,
        semester: sem,
        gender,
        grade,
        age
      };
      appData.stats.students.updated++;
    }
  } else {
    // Check duplicate roll number
    if (appData.students.some(s => s.rollNumber.toLowerCase() === roll.toLowerCase())) {
      alert('A student with this Roll Number already exists.');
      return;
    }
    const newId = appData.students.reduce((max, s) => Math.max(max, s.studentId), 0) + 1;
    appData.students.push({
      studentId: newId,
      rollNumber: roll,
      firstName: fname,
      lastName: lname,
      email: email || `${fname.toLowerCase()}.${lname.toLowerCase()}@example.com`,
      course,
      semester: sem,
      gender,
      grade,
      age,
      dob: '2007-11-20'
    });
    appData.stats.students.total++;
    appData.stats.students.added++;
  }

  saveDataStore();
  renderStudents();
  renderQuickChips();
  closeModals();
}

function deleteStudent(studentId) {
  const s = appData.students.find(st => st.studentId === studentId);
  if (!s) return;
  if (!confirm(`Are you sure you want to delete student ${s.firstName} ${s.lastName} (${s.rollNumber})?`)) return;

  appData.students = appData.students.filter(st => st.studentId !== studentId);
  appData.results = appData.results.filter(r => r.studentId !== studentId);
  appData.stats.students.total--;
  appData.stats.students.deleted++;

  saveDataStore();
  renderStudents();
  renderQuickChips();
  updateResultEntryStats();
}

function viewStudentDetails(studentId) {
  const s = appData.students.find(st => st.studentId === studentId);
  if (!s) return;
  alert(`Student Profile:\nName: ${s.firstName} ${s.lastName}\nRoll Number: ${s.rollNumber}\nCourse: ${s.course}\nSemester: ${s.semester}\nGrade: ${s.grade}\nAge: ${s.age || 19}\nEmail: ${s.email}`);
}

// Subject Modal Management
function openAddSubjectModal() {
  document.getElementById('subject-modal-title').innerText = 'Add Curriculum Subject';
  document.getElementById('subject-form').reset();
  document.getElementById('subject-id-field').value = '';
  document.getElementById('subject-modal').classList.add('active');
}

function openUpdateSubjectModal(subjectId) {
  const sub = appData.subjects.find(s => s.subjectId === subjectId);
  if (!sub) return;

  document.getElementById('subject-modal-title').innerText = 'Update Subject';
  document.getElementById('subject-id-field').value = sub.subjectId;
  document.getElementById('modal-subcode-input').value = sub.subjectCode;
  document.getElementById('modal-subname-input').value = sub.subjectName;
  document.getElementById('modal-maxmarks-input').value = sub.maxMarks;
  document.getElementById('modal-passmarks-input').value = sub.passMarks;
  document.getElementById('modal-credits-input').value = sub.credits || 4;

  document.getElementById('subject-modal').classList.add('active');
}

function saveSubjectForm() {
  const idVal = document.getElementById('subject-id-field').value;
  const code = document.getElementById('modal-subcode-input').value.trim().toUpperCase();
  const name = document.getElementById('modal-subname-input').value.trim();
  const max = parseInt(document.getElementById('modal-maxmarks-input').value, 10) || 100;
  const pass = parseInt(document.getElementById('modal-passmarks-input').value, 10) || 35;
  const credits = parseInt(document.getElementById('modal-credits-input').value, 10) || 4;

  if (!code || !name) {
    alert('Please enter Subject Code and Name.');
    return;
  }

  if (idVal) {
    const subId = parseInt(idVal, 10);
    const idx = appData.subjects.findIndex(s => s.subjectId === subId);
    if (idx >= 0) {
      appData.subjects[idx] = { ...appData.subjects[idx], subjectCode: code, subjectName: name, maxMarks: max, passMarks: pass, credits };
      appData.stats.subjects.updated++;
    }
  } else {
    const newId = appData.subjects.reduce((m, s) => Math.max(m, s.subjectId), 0) + 1;
    appData.subjects.push({ subjectId: newId, subjectCode: code, subjectName: name, maxMarks: max, passMarks: pass, credits });
    appData.stats.subjects.total++;
    appData.stats.subjects.added++;
  }

  saveDataStore();
  renderSubjects();
  setupResultEntry();
  closeModals();
}

function deleteSubject(subjectId) {
  if (!confirm('Are you sure you want to delete this curriculum subject?')) return;
  appData.subjects = appData.subjects.filter(s => s.subjectId !== subjectId);
  appData.results = appData.results.filter(r => r.subjectId !== subjectId);
  appData.stats.subjects.deleted++;
  saveDataStore();
  renderSubjects();
  setupResultEntry();
}

// Quick Select Chips in Marksheet View
function renderQuickChips() {
  const container = document.getElementById('marksheet-quick-chips');
  if (!container) return;

  container.innerHTML = '<span style="font-size:11.5px; font-weight:600; color:var(--text-muted); margin-right:4px;">Quick Select Student:</span>';
  appData.students.forEach(s => {
    const btn = document.createElement('button');
    btn.className = 'quick-chip';
    btn.setAttribute('data-roll', s.rollNumber);
    btn.innerText = `${s.firstName} ${s.lastName} (${s.rollNumber})`;
    btn.onclick = () => {
      document.getElementById('marksheet-roll-input').value = s.rollNumber;
      renderMarksheetView(s.rollNumber);
    };
    container.appendChild(btn);
  });
}

// Export / Download JSON to synchronize back with desktop app
function exportDataJSON() {
  const jsonStr = JSON.stringify({
    students: appData.students,
    subjects: appData.subjects,
    results: appData.results
  }, null, 2);

  const blob = new Blob([jsonStr], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'srms_data.json';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

// Navigation & Tab Switching
function switchTab(tabId) {
  document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
  document.querySelectorAll('.nav-tab-btn').forEach(btn => btn.classList.remove('active'));
  document.querySelectorAll('.sidebar-link').forEach(link => link.classList.remove('active'));

  const targetTab = document.getElementById(tabId);
  if (targetTab) targetTab.classList.add('active');

  const navBtn = document.querySelector(`.nav-tab-btn[data-tab="${tabId}"]`);
  if (navBtn) navBtn.classList.add('active');

  const sideLink = document.querySelector(`.sidebar-link[data-tab="${tabId}"]`);
  if (sideLink) sideLink.classList.add('active');

  // Trigger contextual renders
  if (tabId === 'tab-students') renderStudents();
  if (tabId === 'tab-subjects') renderSubjects();
  if (tabId === 'tab-results-entry') setupResultEntry();
  if (tabId === 'tab-marksheet') {
    renderQuickChips();
    if (!currentMarksheetSummary && appData.students.length > 0) {
      renderMarksheetView(appData.students[3].rollNumber); // Default Mohit Priyadarshi
    }
  }

  // Close mobile sidebar if open
  closeMobileSidebar();
}

function toggleMobileSidebar() {
  const sidebar = document.getElementById('app-sidebar');
  const backdrop = document.getElementById('sidebar-backdrop');
  if (sidebar && backdrop) {
    sidebar.classList.toggle('open');
    backdrop.classList.toggle('open');
  }
}

function closeMobileSidebar() {
  const sidebar = document.getElementById('app-sidebar');
  const backdrop = document.getElementById('sidebar-backdrop');
  if (sidebar && backdrop) {
    sidebar.classList.remove('open');
    backdrop.classList.remove('open');
  }
}

function closeModals() {
  document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
}

// Initial Boot
document.addEventListener('DOMContentLoaded', () => {
  initDataStore();

  // Tab switching events
  document.querySelectorAll('.nav-tab-btn').forEach(btn => {
    btn.addEventListener('click', () => switchTab(btn.getAttribute('data-tab')));
  });

  document.querySelectorAll('.sidebar-link[data-tab]').forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      switchTab(link.getAttribute('data-tab'));
    });
  });

  // Mobile menu events
  const hamburger = document.getElementById('hamburger-toggle');
  if (hamburger) hamburger.addEventListener('click', toggleMobileSidebar);

  const backdrop = document.getElementById('sidebar-backdrop');
  if (backdrop) backdrop.addEventListener('click', closeMobileSidebar);

  // Search input listeners
  const studentSearch = document.getElementById('student-search-input');
  if (studentSearch) {
    studentSearch.addEventListener('input', (e) => renderStudents(e.target.value));
  }

  const subjectSearch = document.getElementById('subject-search-input');
  if (subjectSearch) {
    subjectSearch.addEventListener('input', (e) => renderSubjects(e.target.value));
  }

  // Initial renders
  renderStudents();
  renderSubjects();
  setupResultEntry();
  renderQuickChips();

  // Load default student (Mohit Priyadarshi) on marksheet tab
  renderMarksheetView('2510001320');
});
