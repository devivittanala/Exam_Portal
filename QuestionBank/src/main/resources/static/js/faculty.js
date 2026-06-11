const FACULTY_ID = 1; // Change this after registering faculty
const API_BASE = "http://localhost:8080/api";

async function loadDashboard() {
    try {
        const res = await fetch(`${API_BASE}/questions/faculty/${FACULTY_ID}`);
        const data = await res.json();
        document.getElementById('metric-questions').innerText = data.length;
        renderQuestions(data);
    } catch (e) {
        console.error(e);
    }
}

function renderQuestions(questions) {
    const tbody = document.querySelector('#question-table tbody');
    tbody.innerHTML = '';
    questions.forEach(q => {
        const row = `<tr>
            <td>${q.id}</td>
            <td>${q.question}</td>
            <td>${q.subject}</td>
            <td>${q.marks}</td>
            <td><button onclick="deleteQuestion(${q.id})" style="background:#ef4444">Delete</button></td>
        </tr>`;
        tbody.innerHTML += row;
    });
}

async function submitQuestion() {
    const payload = {
        question: document.getElementById('q-text').value,
        questionType: document.getElementById('q-type').value,
        subject: document.getElementById('q-subject').value,
        topic: "General",
        difficulty: "Medium",
        marks: parseInt(document.getElementById('q-marks').value) || 2,
        facultyId: FACULTY_ID,
        options: [
            {optionText: "Option A", isCorrect: true},
            {optionText: "Option B", isCorrect: false},
            {optionText: "Option C", isCorrect: false},
            {optionText: "Option D", isCorrect: false}
        ]
    };

    const res = await fetch(`${API_BASE}/questions`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });

    if (res.ok) {
        alert("Question Added Successfully!");
        document.getElementById('question-form').reset();
        loadDashboard();
    } else {
        alert("Failed to save question");
    }
}

async function deleteQuestion(id) {
    if (confirm("Delete this question?")) {
        await fetch(`${API_BASE}/questions/${id}/faculty/${FACULTY_ID}`, {method: 'DELETE'});
        loadDashboard();
    }
}

// Load on page load
window.onload = loadDashboard;