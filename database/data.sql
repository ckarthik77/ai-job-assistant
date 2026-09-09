-- Sample Project Data for Resume Enhancement
-- These projects are pre-formatted in Google XYZ format

-- Backend Projects
INSERT INTO projects (title, description, skills, category) VALUES
('E-Commerce Platform with Microservices',
 'Architected a scalable e-commerce platform serving 50K+ daily users as measured by 99.9% uptime and 200ms average response time, by implementing microservices architecture using Spring Boot, Kafka for event streaming, and Redis for caching.',
 '["Java", "Spring Boot", "Microservices", "Kafka", "Redis", "PostgreSQL", "Docker", "Kubernetes"]',
 'backend'),

('RESTful API for Payment Processing',
 'Developed a secure payment processing API handling $2M+ in monthly transactions as measured by zero security breaches and PCI DSS compliance, by implementing OAuth2 authentication, encryption, and comprehensive audit logging with Spring Security.',
 '["Java", "Spring Boot", "Spring Security", "REST API", "OAuth2", "PostgreSQL", "JUnit", "Swagger"]',
 'backend'),

('Real-Time Chat Application',
 'Built a real-time messaging system supporting 10K+ concurrent users as measured by sub-100ms message delivery latency, by implementing WebSocket connections, Redis pub/sub for message broadcasting, and MongoDB for chat history persistence.',
 '["Java", "Spring Boot", "WebSocket", "Redis", "MongoDB", "Docker"]',
 'backend'),

('Inventory Management System',
 'Engineered an inventory tracking system reducing stock discrepancies by 85% as measured by quarterly audits, by implementing barcode scanning, real-time stock updates, automated reorder triggers, and comprehensive reporting dashboards.',
 '["Java", "Spring Boot", "PostgreSQL", "JPA", "REST API", "Docker"]',
 'backend'),

('Authentication Service with JWT',
 'Implemented a centralized authentication service supporting 5 microservices as measured by 100% SSO adoption and reduced auth-related bugs by 90%, by designing JWT-based authentication with refresh tokens, rate limiting, and role-based access control.',
 '["Java", "Spring Boot", "Spring Security", "JWT", "Redis", "PostgreSQL"]',
 'backend');

-- Frontend Projects
INSERT INTO projects (title, description, skills, category) VALUES
('React Dashboard with Real-Time Analytics',
 'Created an analytics dashboard serving 1000+ business users as measured by 45% increase in data-driven decisions, by implementing real-time data visualization using React, D3.js, WebSocket connections, and responsive design principles.',
 '["React", "JavaScript", "D3.js", "WebSocket", "Redux", "Material-UI", "REST API"]',
 'frontend'),

('Progressive Web App for Task Management',
 'Developed a PWA task manager increasing user productivity by 30% as measured by user surveys, by implementing offline-first architecture, push notifications, drag-and-drop interfaces, and cross-device synchronization.',
 '["React", "JavaScript", "PWA", "Service Workers", "IndexedDB", "Material-UI"]',
 'frontend'),

('E-Learning Platform Interface',
 'Built an interactive learning platform with 95% user satisfaction rating as measured by post-course surveys, by designing intuitive course navigation, video streaming integration, progress tracking, and gamification features.',
 '["React", "TypeScript", "Redux", "Video.js", "Tailwind CSS", "Axios"]',
 'frontend'),

('Component Library with Storybook',
 'Established a reusable component library adopted by 8 development teams as measured by 40% reduction in UI development time, by creating 50+ documented components with accessibility compliance, unit tests, and interactive documentation.',
 '["React", "TypeScript", "Storybook", "Jest", "React Testing Library", "CSS Modules"]',
 'frontend'),

('Single Page Application with Dynamic Routing',
 'Architected a SPA improving page load times by 60% as measured by Lighthouse scores, by implementing code splitting, lazy loading, optimized bundle sizes, and client-side routing with React Router.',
 '["React", "JavaScript", "React Router", "Webpack", "Redux", "Axios"]',
 'frontend');

-- Full Stack Projects
INSERT INTO projects (title, description, skills, category) VALUES
('Social Media Platform',
 'Launched a social platform reaching 50K users in 3 months as measured by active monthly users, by building a full-stack application with React frontend, Spring Boot backend, PostgreSQL database, Redis caching, and AWS S3 for media storage.',
 '["React", "Java", "Spring Boot", "PostgreSQL", "Redis", "AWS S3", "Docker", "Nginx"]',
 'fullstack'),

('Job Board Application',
 'Developed a job matching platform connecting 5K+ candidates with employers as measured by 500+ successful placements, by implementing advanced search filters, resume parsing, email notifications, and application tracking with full-stack architecture.',
 '["React", "Java", "Spring Boot", "PostgreSQL", "Elasticsearch", "Redis", "Docker"]',
 'fullstack'),

('Healthcare Appointment System',
 'Created a telemedicine booking system reducing appointment scheduling time by 75% as measured by average booking duration, by integrating calendar APIs, video conferencing, payment processing, and HIPAA-compliant data storage.',
 '["React", "Java", "Spring Boot", "PostgreSQL", "Stripe API", "WebRTC", "Docker"]',
 'fullstack'),

('Food Delivery Platform',
 'Built an end-to-end delivery platform processing 1K+ daily orders as measured by order volume, by implementing real-time order tracking, Google Maps integration, payment gateway, restaurant dashboard, and delivery partner app.',
 '["React Native", "Node.js", "Express", "MongoDB", "Socket.io", "Google Maps API", "Stripe"]',
 'fullstack');

-- Machine Learning Projects
INSERT INTO projects (title, description, skills, category) VALUES
('Resume Parser with NLP',
 'Engineered an ML-powered resume parser achieving 92% extraction accuracy as measured by validation tests, by implementing NER models, skill taxonomy matching, and structured data extraction using spaCy and transformers.',
 '["Python", "NLP", "spaCy", "Transformers", "scikit-learn", "FastAPI", "PostgreSQL"]',
 'ml'),

('Recommendation Engine',
 'Built a collaborative filtering recommendation system increasing user engagement by 35% as measured by session duration, by implementing matrix factorization, content-based filtering, and A/B testing framework.',
 '["Python", "scikit-learn", "pandas", "NumPy", "Flask", "Redis", "PostgreSQL"]',
 'ml'),

('Sentiment Analysis API',
 'Developed a sentiment analysis service processing 100K+ texts daily as measured by API requests, by fine-tuning BERT models, implementing REST API endpoints, caching predictions, and monitoring model performance.',
 '["Python", "Transformers", "PyTorch", "FastAPI", "Redis", "Docker"]',
 'ml'),

('Image Classification System',
 'Created a computer vision system achieving 94% classification accuracy as measured by test dataset, by training CNN models, implementing data augmentation pipelines, and deploying with FastAPI and GPU acceleration.',
 '["Python", "TensorFlow", "Keras", "OpenCV", "FastAPI", "Docker", "AWS"]',
 'ml');

-- DevOps Projects
INSERT INTO projects (title, description, skills, category) VALUES
('CI/CD Pipeline Automation',
 'Established automated deployment pipeline reducing release time by 80% as measured by deployment duration, by implementing Jenkins pipelines, Docker containerization, Kubernetes orchestration, and automated testing workflows.',
 '["Jenkins", "Docker", "Kubernetes", "Git", "Bash", "Terraform", "AWS"]',
 'devops'),

('Infrastructure as Code Implementation',
 'Migrated infrastructure to IaC reducing provisioning errors by 95% as measured by incident reports, by implementing Terraform modules, automated testing, state management, and multi-environment deployments.',
 '["Terraform", "AWS", "Docker", "Kubernetes", "Git", "Python", "Bash"]',
 'devops'),

('Monitoring and Alerting System',
 'Deployed comprehensive monitoring reducing MTTR by 60% as measured by incident resolution time, by implementing Prometheus metrics, Grafana dashboards, ELK stack for logs, and PagerDuty integration for alerts.',
 '["Prometheus", "Grafana", "ELK Stack", "Docker", "Kubernetes", "Python"]',
 'devops'),

('Container Orchestration Platform',
 'Architected Kubernetes cluster serving 20+ microservices as measured by services migrated, by implementing auto-scaling, service mesh with Istio, centralized logging, and zero-downtime deployments.',
 '["Kubernetes", "Docker", "Istio", "Helm", "Prometheus", "AWS EKS"]',
 'devops');

-- Mobile Projects
INSERT INTO projects (title, description, skills, category) VALUES
('Cross-Platform Mobile App',
 'Launched a mobile app achieving 4.5+ star rating with 10K+ downloads as measured by app store metrics, by building cross-platform application using React Native, implementing push notifications, offline sync, and in-app purchases.',
 '["React Native", "JavaScript", "Redux", "Firebase", "REST API", "iOS", "Android"]',
 'mobile'),

('Fitness Tracking Application',
 'Developed a fitness app with 5K+ active users as measured by DAU, by implementing step counting, GPS route tracking, workout analytics, social features, and health kit integration.',
 '["React Native", "JavaScript", "Redux", "Firebase", "Google Maps API", "HealthKit"]',
 'mobile'),

('Real-Time Location Sharing App',
 'Built a location-based app serving 2K+ concurrent users as measured by real-time connections, by implementing WebSocket communication, map visualization, geofencing, and battery-optimized location tracking.',
 '["React Native", "JavaScript", "Socket.io", "Google Maps API", "Firebase"]',
 'mobile');

-- Data Engineering Projects
INSERT INTO projects (title, description, skills, category) VALUES
('ETL Pipeline for Analytics',
 'Engineered data pipeline processing 5M+ records daily as measured by throughput, by implementing Apache Airflow workflows, data validation, transformation logic, and loading into data warehouse with 99.9% reliability.',
 '["Python", "Apache Airflow", "PostgreSQL", "Snowflake", "pandas", "SQL"]',
 'data'),

('Real-Time Data Streaming Platform',
 'Built streaming data platform reducing data latency from hours to seconds as measured by end-to-end lag, by implementing Kafka producers/consumers, stream processing with Kafka Streams, and real-time analytics dashboards.',
 '["Kafka", "Java", "Spring Boot", "PostgreSQL", "Elasticsearch", "Grafana"]',
 'data'),

('Data Warehouse Optimization',
 'Optimized data warehouse reducing query times by 70% as measured by query performance metrics, by implementing partitioning strategies, materialized views, indexing optimization, and query rewriting.',
 '["SQL", "PostgreSQL", "Python", "dbt", "Apache Airflow", "Redshift"]',
 'data'),

('Business Intelligence Dashboard',
 'Created executive dashboard consolidating 15+ data sources as measured by integrated systems, by implementing ETL processes, data modeling, calculated metrics, and interactive visualizations with drill-down capabilities.',
 '["SQL", "Python", "Tableau", "PostgreSQL", "Apache Airflow", "REST API"]',
 'data');

-- Add timestamp
INSERT INTO projects (title, description, skills, category) VALUES
('API Gateway Implementation',
 'Deployed centralized API gateway handling 1M+ requests daily as measured by traffic volume, by implementing rate limiting, authentication, request routing, load balancing, and comprehensive API documentation.',
 '["Java", "Spring Cloud Gateway", "Redis", "Kubernetes", "Prometheus", "JWT"]',
 'backend');
