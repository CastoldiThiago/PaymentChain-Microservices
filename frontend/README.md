# Frontend React para PaymentChain

Este frontend permite probar el backend de microservicios bancarios con autenticación Keycloak.

## Estructura

- src/
  - auth/
    - keycloak.ts
    - AuthProvider.tsx
    - useAuth.ts
  - services/
    - axiosInstance.ts
    - customer.service.ts
    - account.service.ts
    - transaction.service.ts
  - pages/
    - Login.tsx
    - Customers.tsx
    - Accounts.tsx
    - Transactions.tsx
  - routes/
    - ProtectedRoute.tsx
  - App.tsx
  - main.tsx
- .env
- vite.config.ts
- package.json
- tsconfig.json

## Instalación y ejecución

1. Copia `.env.example` a `.env` y ajusta las URLs.
2. Ejecuta `npm install`.
3. Ejecuta `npm run dev`.

## Funcionalidad
- Login con Keycloak
- CRUD básico de Customers, Accounts, Transactions
- Probar backend bancario fácilmente
