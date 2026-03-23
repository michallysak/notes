# Authentication & User Business Logic: MoSCoW Plan (as of 2026-03-23)

## Must Have (core requirements for secure authentication)
- [ ] User registration and login with unique email and strong password
- [ ] Secure password verification and storage (e.g. PBKDF2, no legacy types)
- [ ] Password policy always enforced (registration, security migration)
- [ ] No user existence leak or sensitive info in login failure or logs

## Should Have (improvements for security and user experience)
- [ ] Basic security migration (increase iterations, rehash on login)
- [ ] Session management (auth tokens, expiration)
- [ ] User credentials update (change password)
- [ ] Role-based authorization

## Could Have / Won't Have (for now)
- [ ] Minimal brute-force protection (constant-time check, timing delay)
- [ ] Password reset/recovery
- [ ] Account lockout or advanced rate limiting
- [ ] Audit logging for login/registration
- [ ] Email verification on registration
- [ ] Support for legacy/alternative credential types

---
This MoSCoW plan reflects the current and planned business logic for authentication and user management. "Must Have" items are required for a secure, production-ready baseline. "Should Have" and "Could Have" are recommended for improved security and usability. "Won't Have" are explicitly out of scope for now.
