# Hotel REST API

RESTful API для управления гостиницами. Реализован на **Spring Boot 3 / Java 21** с поддержкой двух баз данных (H2 по умолчанию, PostgreSQL через профиль), автоматическими миграциями Liquibase и документацией Swagger.

---

## Технологический стек

| Слой | Технология |
|---|---|
| Язык | Java 21 |
| Фреймворк | Spring Boot 3.2.5 |
| ORM | Spring Data JPA / Hibernate 6 |
| Миграции БД | Liquibase (YAML) |
| БД по умолчанию | H2 (in-memory) |
| Альтернативная БД | PostgreSQL |
| Валидация | Jakarta Bean Validation |
| Документация | SpringDoc OpenAPI 2.5.0 (Swagger UI) |
| Сборка | Apache Maven |
| Утилиты | Lombok |
| Тесты | JUnit 5, MockMvc |

---

## Требования

- **Java 21** (JDK)
- **Apache Maven 3.8+**
- Интернет-соединение (для загрузки зависимостей из Maven Central)

Для PostgreSQL-профиля дополнительно нужен запущенный PostgreSQL-сервер.

---

## Быстрый старт

```bash
# 1. Клонировать репозиторий
git clone <repo-url>
cd hotel-api

# 2. Собрать и запустить (H2 in-memory, никаких настроек не нужно)
mvn spring-boot:run
```

Сервер поднимается на **`http://localhost:8092`**.

### Запуск тестов

```bash
mvn test
```

---

## База данных

### H2 (по умолчанию)

Используется автоматически без дополнительной настройки.

- Консоль H2: `http://localhost:8092/h2-console`
- JDBC URL: `jdbc:h2:mem:hoteldb`
- Пользователь: `sa`, пароль: *(пусто)*

### PostgreSQL

```bash
# Создать базу данных
createdb hoteldb

# Запустить с PostgreSQL-профилем
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Переменные окружения для подключения (опционально):

```bash
export DB_USER=postgres
export DB_PASSWORD=postgres
```

По умолчанию используются `postgres` / `postgres`.

---

## Структура проекта

```
src/main/java/com/hotel/api/
├── controller/          # REST-контроллеры (@RestController)
├── service/             # Бизнес-логика (интерфейс + impl)
├── repository/          # Spring Data JPA репозитории
├── entity/              # JPA-сущности (Hotel, Amenity, Address, ...)
├── dto/
│   ├── request/         # Входящие DTO (HotelCreateRequest, ...)
│   └── response/        # Исходящие DTO (HotelBriefResponse, HotelDetailResponse, ...)
├── mapper/              # Маппинг entity ↔ DTO
├── specification/       # JPA Specification для динамического поиска
└── exception/           # Кастомные исключения и GlobalExceptionHandler

src/main/resources/
├── application.yml              # Основная конфигурация (H2)
├── application-postgres.yml     # PostgreSQL-профиль
└── db/changelog/                # Liquibase-миграции (YAML)
```

---

## API Endpoints

Базовый префикс всех эндпоинтов: **`/property-view`**

| Метод | URL | Описание |
|---|---|---|
| `GET` | `/property-view/hotels` | Список всех отелей (краткая информация) |
| `GET` | `/property-view/hotels/{id}` | Детальная информация об отеле по ID |
| `GET` | `/property-view/search` | Поиск отелей по фильтрам |
| `POST` | `/property-view/hotels` | Создать новый отель |
| `POST` | `/property-view/hotels/{id}/amenities` | Добавить удобства к отелю |
| `GET` | `/property-view/histogram/{param}` | Гистограмма отелей по параметру |

---

### GET `/property-view/hotels`

Возвращает список всех отелей в кратком формате.

**Ответ `200 OK`:**
```json
[
  {
    "id": 1,
    "name": "DoubleTree by Hilton Minsk",
    "description": "Современный отель в центре Минска",
    "brand": "Hilton",
    "address": "9 Pobediteley Avenue, Minsk, 220004, Belarus",
    "phone": "+375 17 309-80-00"
  }
]
```

---

### GET `/property-view/hotels/{id}`

Детальная информация об отеле включая удобства и время заезда/выезда.

**Ответ `200 OK`:**
```json
{
  "id": 1,
  "name": "DoubleTree by Hilton Minsk",
  "brand": "Hilton",
  "description": "Современный отель в центре Минска",
  "address": {
    "houseNumber": "9",
    "street": "Pobediteley Avenue",
    "city": "Minsk",
    "country": "Belarus",
    "postCode": "220004"
  },
  "contacts": {
    "phone": "+375 17 309-80-00",
    "email": "doubletree.minsk@hilton.com"
  },
  "arrivalTime": {
    "checkIn": "14:00",
    "checkOut": "12:00"
  },
  "amenities": ["Free Wi-Fi", "Parking", "Gym"]
}
```

**Ошибка `404 Not Found`** — если отель с таким ID не найден.

---

### GET `/property-view/search`

Динамический поиск. Все параметры опциональны и комбинируются через `AND`.

| Параметр | Тип | Описание |
|---|---|---|
| `name` | `String` | Поиск по названию (LIKE, без учёта регистра) |
| `brand` | `String` | Поиск по бренду (LIKE, без учёта регистра) |
| `city` | `String` | Поиск по городу (LIKE, без учёта регистра) |
| `country` | `String` | Поиск по стране (LIKE, без учёта регистра) |
| `amenities` | `List<String>` | Отели, у которых есть хотя бы одно из указанных удобств |

**Пример запроса:**
```
GET /property-view/search?city=Minsk&amenities=Gym,Parking
```

**Ответ `200 OK`** — список `HotelBriefResponse` (аналогично `/hotels`).

---

### POST `/property-view/hotels`

Создать новый отель.

**Тело запроса:**
```json
{
  "name": "DoubleTree by Hilton Minsk",
  "description": "Современный отель в центре Минска",
  "brand": "Hilton",
  "address": {
    "houseNumber": "9",
    "street": "Pobediteley Avenue",
    "city": "Minsk",
    "country": "Belarus",
    "postCode": "220004"
  },
  "contacts": {
    "phone": "+375 17 309-80-00",
    "email": "doubletree.minsk@hilton.com"
  },
  "arrivalTime": {
    "checkIn": "14:00",
    "checkOut": "12:00"
  }
}
```

**Ответ `201 Created`** — созданный отель в кратком формате (`HotelBriefResponse`).

**Ошибка `400 Bad Request`** — если не переданы обязательные поля (`name`, `address`, `contacts`, `arrivalTime`).

---

### POST `/property-view/hotels/{id}/amenities`

Добавить удобства к существующему отелю. Дубликаты игнорируются автоматически.

**Тело запроса:**
```json
["Free Wi-Fi", "Parking", "Gym"]
```

**Ответ `200 OK`** — тело ответа отсутствует.

**Ошибка `404 Not Found`** — если отель с таким ID не найден.

---

### GET `/property-view/histogram/{param}`

Возвращает количество отелей, сгруппированных по указанному параметру.

**Допустимые значения `{param}`:**

| Значение | Группировка |
|---|---|
| `brand` | По бренду |
| `city` | По городу |
| `country` | По стране |
| `amenities` | По удобствам |

**Пример ответа для `/histogram/city`:**
```json
{
  "Minsk": 3,
  "Warsaw": 2,
  "Berlin": 1
}
```

**Ошибка `400 Bad Request`** — если передан неизвестный параметр.

---

## Обработка ошибок

Все ошибки возвращаются в формате [RFC 7807 Problem Detail](https://datatracker.ietf.org/doc/html/rfc7807):

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Hotel with id 999 not found",
  "instance": "/property-view/hotels/999"
}
```

---

## Swagger UI

После запуска документация API доступна по адресу:

```
http://localhost:8092/swagger-ui.html
```

OpenAPI JSON-схема:
```
http://localhost:8092/api-docs
```

---

## Миграции базы данных

Схема БД управляется **Liquibase** и применяется автоматически при старте приложения.

Файлы миграций: `src/main/resources/db/changelog/`

- `001-initial-schema.yaml` — создание таблиц `hotels`, `amenities`, `hotel_amenities`

Liquibase использует YAML-формат вместо SQL, что обеспечивает совместимость с любой поддерживаемой базой данных без изменений.

---

## Тесты

Интеграционные тесты покрывают все 6 эндпоинтов:

```bash
mvn test
```

- Используют `@SpringBootTest` + `MockMvc` — поднимают полный контекст Spring
- Перед каждым тестом БД очищается через `@BeforeEach` (без пересоздания контекста)
- Тесты не зависят друг от друга и могут запускаться в любом порядке
