AI-Powered Job Application Tracker (Full-stack)

An intelligent web application designed to help job seekers organize their applications and get AI-driven insights into their job descriptions using local LLMs.

🚀 Overview

Managing job applications can be overwhelming. This project provides a centralized dashboard to track status (Applied, Interview, Rejected, Offer), store job descriptions, and use AI (Ollama) to analyze job requirements and summarize key points directly from the browser.

✨ Key Features

Full CRUD Functionality: Create, Read, Update, and Delete job applications.

AI Matching Score: Integration with Ollama (Llama 3/Mistral) to analyze CVs against job descriptions and calculate a percentage match score.

Smart Skill Gap Analysis: AI-driven insights that highlight missing skills and provide tips for interview preparation based on the job post.

Dynamic Dashboard: Real-time statistics on your application progress.

Responsive UI: Built with React and Tailwind CSS for a seamless experience on all devices.

Secure Backend: Spring Boot REST API with PostgreSQL for robust data persistence.

🛠 Tech Stack

Frontend: React.js, Tailwind CSS, Lucide Icons, Axios.

Backend: Java 17+, Spring Boot (Web, Data JPA, Validation).

Database: PostgreSQL.

AI Integration: Ollama (Local LLM API).

Build Tools: Maven, NPM.

⚙️ Installation & Setup

Prerequisites

Java 17 or higher

Node.js & NPM (v18+ recommended)

PostgreSQL (Running on port 5432)

Ollama installed and running locally (Download here)

1. Backend Setup

Navigate to the root folder.

Configure your database in src/main/resources/application.properties:

spring.datasource.url=jdbc:postgresql://localhost:5432/job_portal_db
spring.datasource.username=postgres
spring.datasource.password=your_password



Build and run the application:

./mvnw spring-boot:run



2. Frontend Setup

Navigate to the /frontend folder.

Install dependencies:

npm install



Start the development server:

npm run dev



3. AI Setup (Ollama)

Ensure the Ollama service is active and pull the Llama 3 model:

ollama pull llama3
ollama serve



🧠 How it Works

The Backend: Actively manages job data through RESTful endpoints. It utilizes Spring Data JPA for seamless interaction with the PostgreSQL database and handles the orchestration of AI requests.

The AI Component: The system leverages Spring AI to communicate with a local Ollama instance. It uses custom prompts to compare the user's CV text with the job description, returning a structured Matching Percentage and feedback.

The Frontend: A reactive SPA (Single Page Application) built with Vite and React. It handles file reading for CVs and displays the AI results in an interactive "Match Result" card.

📈 Future Improvements

[ ] User Authentication: Implementing Spring Security with JWT for private user accounts.

[ ] Cloud Storage: Integration with AWS S3 for persistent CV and document storage.

[ ] Advanced Matching: Fine-tuning AI prompts for even more accurate skill-gap analysis.

[ ] Dark Mode: Adding a theme switcher for better accessibility.

