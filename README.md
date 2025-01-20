# API Documentation

## Overview
Это документация для API. 
## Эндпоинты

### 1. Регистрация (/register)

- **Метод:** POST
- **URL:** `http://localhost:8000/register`
- **Заголовки:**
  - `Content-Type: application/json`
- **Тело запроса:**
```json
{
    "body": {
      "username": "user",
      "password": "password"
    }
}
```

```
curl -X POST -H "Content-Type: application/json" -d '{"username":"user","password":"password"}' http://localhost:8000/register
```

### 2. Авторизация (/auth)

- **Метод:** POST
- **URL:** `http://localhost:8000/auth`
- **Заголовки:**
  - `Content-Type: application/json`
- **Тело запроса:**
```json
{
    "body": {
      "username": "user",
      "password": "password"
    }
}
```

```
curl -X POST -H "Content-Type: application/json" -d '{"username":"user","password":"password"}' http://localhost:8000/auth
```

### 3. Создание кошелька (/wallet)

- **Метод:** POST
- **URL:** `http://localhost:8000/wallet`
- **Заголовки:**
  - `Content-Type: application/json`
- **Тело запроса:**
```json
{
    "body": {
      "uuid": "user-uuid",
      "name": "My Wallet",
      "initialBalance": 1000.0
    }
}
```

```
curl -X POST -H "Content-Type: application/json" -d '{"uuid":"user-uuid","name":"My Wallet","initialBalance":1000.0}' http://localhost:8000/wallet
```

### 4. Получение кошельков пользователя (/wallet)

- **Метод:** GET
- **URL:** `http://localhost:8000/wallet?uuid=user-uuid`

```
curl -X GET "http://localhost:8000/wallet?uuid=user-uuid"
```

### 5. Создание категории (/category)

- **Метод:** POST
- **URL:** `http://localhost:8000/category`
- **Заголовки:**
  - `Content-Type: application/json`
- **Тело запроса:**
```json
{
    {
      "uuid": "user-uuid",
      "walletId": 1,
      "name": "Food",
      "budget": 500.0
    }
}
```

```
curl -X POST -H "Content-Type: application/json" -d '{"uuid":"user-uuid","walletId":1,"name":"Food","budget":500.0}' http://localhost:8000/category
```

### 6. Получение категорий кошелька (/category)

- **Метод:** DELETE
- **URL:** `http://localhost:8000/category?uuid=user-uuid&walletId=1`

```
curl -X GET "http://localhost:8000/category?uuid=user-uuid&walletId=1
```

### 7. Изменение бюджета категории (/category)

- **Метод:** PUT
- **URL:** `http://localhost:8000/category`
- **Заголовки:**
  - `Content-Type: application/json`
- **Тело запроса:**
```json
{
    {
      "uuid": "user-uuid",
      "categoryId": 1,
      "newBudget": 1000.0
    }
}
```

```
curl -X PUT -H "Content-Type: application/json" -d '{"uuid":"user-uuid","categoryId":1,"newBudget":1000.0}' http://localhost:8000/category
```

### 8. Создание транзакции (/transaction)

- **Метод:** POST
- **URL:** `http://localhost:8000/transaction`
- **Заголовки:**
  - `Content-Type: application/json`
- **Тело запроса:**
```json
{
    {
      "uuid": "user-uuid",
      "categoryId": 1,
      "walletId": 1,
      "type": "expense",
      "amount": 100.0,
      "description": "Покупка продуктов"
    }
}
```

```
curl -X POST http://localhost:8000/transaction -H "Content-Type: application/json" -d '{"uuid":"user-uuid","categoryId":1,"walletId":1,"type":"expense","amount":100.0,"description":"Покупка продуктов"}'
```

### 9. Получение транзакции по кошельку (/transaction)

- **Метод:** GET
- **URL:** `http://localhost:8000/transaction?uuid=user-uuid&walletId=1`

```
curl -X GET "http://localhost:8000/transaction?uuid=user-uuid&walletId=1"
```

### 9. Получение транзакции по категории (/transaction)

- **Метод:** GET
- **URL:** `http://localhost:8000/transaction?uuid=user-uuid&categoryId=1`

```
curl -X GET "http://localhost:8000/transaction?uuid=user-uuid&categoryId=1"
```
