# Cordova Plugin RuStore Pay

Cordova плагин для интеграции с платежной системой RuStore. Позволяет осуществлять покупки внутри приложений, получать информацию о продуктах и управлять платежами через российский магазин приложений RuStore.

## 📋 Содержание
- [Установка](#установка)
- [Поддерживаемые платформы](#поддерживаемые-платформы)
- [Быстрый старт](#быстрый-старт)
- [API](#api)
  - [Основные методы](#основные-методы)
  - [Дополнительные методы](#дополнительные-методы)
- [Примеры использования](#примеры-использования)
- [Типы данных](#типы-данных)
- [Обработка ошибок](#обработка-ошибок)
- [Важные особенности](#️-важные-особенности)

## 🚀 Установка

```bash
cordova plugin add ../cordova-plugin-rustore-pay/
```
или
```bash
cordova plugin add cordova-plugin-rustore-pay
```

## 📱 Поддерживаемые платформы

- Android

## ⚡ Быстрый старт

### 1. Настройка после установки плагина

⚠️ **ВАЖНО**: После установки плагина обязательно замените тестовые параметры на реальные в файле `plugins/cordova-plugin-rustore-pay/plugin.xml`:

**Найдите и замените следующие параметры:**

```xml
<!-- ЗАМЕНИТЕ НА РЕАЛЬНЫЙ ID ПРИЛОЖЕНИЯ ИЗ RUSTORE -->
<meta-data android:name="console_app_id_value" android:value="12345678" />

<!-- DEEP LINK ПРИЛОЖЕНИЯ - замените на уникальную схему вашего приложения -->  
<meta-data android:name="sdk_pay_scheme_value" android:value="com.demo.stand.pay" />

<!-- ЗАМЕНИТЕ НА РЕАЛЬНЫЙ ID ПРИЛОЖЕНИЯ ИЗ RUSTORE -->
<string name="console_app_id_value">12345678</string>
```

**На ваши реальные значения:**

1. **App ID**: Получите в [RuStore Console](https://console.rustore.ru) → Можно найти в адресной строке
2. **Deep Link схема**: Используйте уникальную схему вида `com.yourcompany.yourapp.pay`

### 2. Использование в коде

```javascript
// Проверка доступности платежей
const purchasesAvailable = await RustorePay.getPurchaseAvailability({});
if (!purchasesAvailable.available) {
    console.error('Магазин RuStore не доступен');
    return;
}

// Получение информации о продуктах
const products = await RustorePay.getProducts(...);

// Покупка продукта
const purchase = await RustorePay.purchase({
    productId: "premium_subscription",
    quantity: 1
});

console.log("Покупка завершена:", purchase.purchaseId);
```

## 🎥 Демонстрация

Посмотрите, как работает плагин на практике:

[🎬 Посмотреть демонстрацию работы плагина](media/demo.mp4)

*Видео показывает основные функции плагина: получение продуктов, совершение покупки и получение списка покупок.*

## 📚 API

### Основные методы

### 🛒 `purchase(params)`

Осуществляет покупку продукта через RuStore.

**Параметры:**
```typescript
{
  productId: string;    // ID продукта (обязательный)
  quantity?: number;    // Количество (по умолчанию: 1)
}
```

**Возвращает:**
```typescript
Promise<{
  purchaseId: string;   // ID покупки
  productId: string;    // ID продукта
  invoiceId: string;    // ID счета
}>
```

**Пример:**
```javascript
try {
    const result = await RustorePay.purchase({
        productId: "premium_subscription",
        quantity: 1
    });
    console.log("Покупка успешна:", result.purchaseId);
} catch (error) {
    console.error("Ошибка покупки:", error);
}
```

### 📦 `getProducts(params)`

Получает информацию о продуктах из RuStore.

**Параметры:**
```typescript
{
  productIds: string[]; // Массив ID продуктов (обязательный)
}
```

**Возвращает:**
```typescript
Promise<{
  products: Array<{
    productId: string;    // ID продукта
    type: string;         // Тип продукта
    amountLabel: string;  // Отформатированная цена
    price: number;        // Цена в копейках
    currency: string;     // Валюта
    imageUrl: string;     // URL изображения
    title: string;        // Название
    description: string;  // Описание
  }>
}>
```

**Пример:**
```javascript
const result = await RustorePay.getProducts({
    productIds: ["premium", "coins_100", "remove_ads"]
});

result.products.forEach(product => {
    console.log(`${product.title}: ${product.amountLabel}`);
});
```

### 🧾 `getPurchases(params)`

Получает список покупок пользователя с возможностью фильтрации.

**Параметры (опциональные):**
```typescript
{
  productType?: string;     // Тип продукта для фильтрации
  purchaseStatus?: string;  // Статус покупки (требует указания productType)
}
```

**Возвращает:**
```typescript
Promise<{
  purchases: Array<{
    // Общие поля для всех типов покупок (интерфейс Purchase):
    purchaseId: string;          // ID покупки
    invoiceId: string;           // ID счета
    purchaseTime: number;        // Время покупки (timestamp)
    orderId: string;             // ID заказа
    purchaseType: string;        // Тип покупки
    description: string;         // Описание
    amountLabel: string;         // Отформатированная сумма
    price: number;               // Цена в копейках
    currency: string;            // Валюта
    status: string;              // Статус покупки
    developerPayload: string;    // Дополнительные данные разработчика
    sandbox: boolean;            // Тестовая покупка

    // Специфичные поля для CONSUMABLE и NON_CONSUMABLE (ProductPurchase):
    productId?: string;          // ID продукта
    quantity?: number;           // Количество
    productType?: string;        // Тип продукта (CONSUMABLE_PRODUCT/NON_CONSUMABLE_PRODUCT)

    // Специфичные поля для SUBSCRIPTION (SubscriptionPurchase):
    productId?: string;          // ID продукта подписки
    expirationDate?: number;     // Дата истечения подписки (timestamp)
    gracePeriodEnabled?: boolean; // Льготный период включен

    // Вспомогательное поле:
    type?: string;               // "PRODUCT" для товаров или "SUBSCRIPTION" для подписок
  }>
}>
```

**Фильтрация:**

Метод поддерживает фильтрацию покупок по типу продукта и статусу:

1. **По типу продукта** (`productType`):
   - `CONSUMABLE` - расходуемые товары
   - `NON_CONSUMABLE` - нерасходуемые товары
   - `SUBSCRIPTION` - подписки

2. **По статусу покупки** (`purchaseStatus`):

   ⚠️ **Важно:** Параметр `purchaseStatus` можно использовать только при указании `productType`, так как разные типы продуктов имеют разные статусы.

   **Для CONSUMABLE и NON_CONSUMABLE:**
   - `INVOICE_CREATED` - счёт создан
   - `PAID` - оплачено
   - `CONFIRMED` - подтверждено
   - `CANCELLED` - отменено
   - `REFUNDED` - возвращено
   - `REJECTED` - отклонено

   **Для SUBSCRIPTION:**
   - `INVOICE_CREATED` - счёт создан
   - `ACTIVE` - активна
   - `CANCELLED` - отменена
   - `PAUSED` - приостановлена
   - `EXPIRED` - истекла

**Примеры:**

```javascript
// Получить все покупки без фильтрации
const allPurchases = await RustorePay.getPurchases({});
console.log(`Всего покупок: ${allPurchases.purchases.length}`);

// Получить только расходуемые товары
const consumables = await RustorePay.getPurchases({
    productType: 'CONSUMABLE'
});

// Получить только активные подписки
const activeSubscriptions = await RustorePay.getPurchases({
    productType: 'SUBSCRIPTION',
    purchaseStatus: 'ACTIVE'
});
console.log(`Активных подписок: ${activeSubscriptions.purchases.length}`);

// Получить оплаченные нерасходуемые товары
const paidNonConsumables = await RustorePay.getPurchases({
    productType: 'NON_CONSUMABLE',
    purchaseStatus: 'PAID'
});

// Обработка результатов
allPurchases.purchases.forEach(purchase => {
    const date = new Date(purchase.purchaseTime);
    console.log(`Покупка: ${purchase.purchaseId}`);
    console.log(`  Продукт: ${purchase.productId || 'N/A'}`);
    console.log(`  Статус: ${purchase.status}`);
    console.log(`  Тип: ${purchase.type || purchase.purchaseType}`);
    console.log(`  Куплено: ${date.toLocaleDateString()}`);
    console.log(`  Сумма: ${purchase.amountLabel}`);
    console.log(`  Тестовая: ${purchase.sandbox ? 'Да' : 'Нет'}`);
});
```

**Обработка ошибок:**

```javascript
try {
    // Ошибка: purchaseStatus без productType
    await RustorePay.getPurchases({
        purchaseStatus: 'PAID' // ❌ Ошибка!
    });
} catch (error) {
    console.error(error); // "purchaseStatus can only be used when productType is specified"
}

// Правильно:
const purchases = await RustorePay.getPurchases({
    productType: 'CONSUMABLE',
    purchaseStatus: 'PAID' // ✅ OK
});
```

### Дополнительные методы

### 👤 `getUserAuthorizationStatus()`
Получает статус авторизации пользователя в RuStore.

### ✅ `getPurchaseAvailability()`
Проверяет доступность платежей.

### 📱 `openRuStore()`
Открывает приложение RuStore.

### 📥 `openRuStoreDownloadInstruction()`
Показывает инструкцию по установке RuStore.

## 💡 Примеры использования

### Проверка доступности платежей

```javascript
const availability = await RustorePay.getPurchaseAvailability({});
if (availability.available) {
    console.log("Платежи доступны");
} else {
    console.error("Платежи недоступны:", availability.error);
}
```

### Покупка с обработкой ошибок

```javascript
async function buyPremium() {
    try {
        // Проверяем авторизацию
        const auth = await RustorePay.getUserAuthorizationStatus({});
        if (!auth.isAuthorized) {
            console.error("Пользователь не авторизован в RuStore");
            return;
        }

        // Получаем информацию о продукте
        const products = await RustorePay.getProducts({
            productIds: ["premium_subscription"]
        });
        
        if (products.products.length === 0) {
            throw new Error("Продукт не найден");
        }

        // Совершаем покупку
        const purchase = await RustorePay.purchase({
            productId: "premium_subscription"
        });

        console.log("Премиум активирован:", purchase.purchaseId);
        
    } catch (error) {
        console.error("Ошибка покупки:", error);
    }
}
```

### Восстановление покупок

```javascript
async function restorePurchases() {
    try {
        const result = await RustorePay.getPurchases({});
        
        const premiumPurchases = result.purchases.filter(p => 
            p.description.includes("premium")
        );
        
        if (premiumPurchases.length > 0) {
            console.log("Премиум подписка найдена");
            activatePremiumFeatures();
        }
        
    } catch (error) {
        console.error("Ошибка восстановления:", error);
    }
}
```

## 📊 Типы данных

### Типы продуктов
- `CONSUMABLE_PRODUCT` - Расходуемый товар
- `NON_CONSUMABLE_PRODUCT` - Нерасходуемый товар  
- `SUBSCRIPTION` - Подписка

### Статусы авторизации
- `authorized` - Пользователь авторизован
- `unauthorized` - Пользователь не авторизован
- `unknown` - Неизвестный статус

## ⚠️ Обработка ошибок

Все методы плагина возвращают Promise и могут генерировать ошибки. Рекомендуется всегда использовать try/catch блоки:

```javascript
try {
    const result = await RustorePay.purchase({productId: "test"});
    // Обработка успешной покупки
} catch (error) {
    // Обработка ошибок
    console.error("Ошибка:", error.message);
}
```

Типичные ошибки:
- RuStore не установлен на устройстве
- Пользователь не авторизован
- Продукт не найден
- Платежи недоступны
- Пользователь отменил покупку

## ⚠️ Важные особенности

### Подпись приложения
- **Платежи НЕ работают в debug версии приложения**
- Подпись вашего APK должна точно совпадать с подписью, загруженной в RuStore Console
- Для тестирования используйте только release-сборки с правильной подписью
- Подробнее: [Проверка подписи приложения](https://www.rustore.ru/help/guides/check-sign)

### Полезные ссылки
- [Тестирование платежного SDK](https://www.rustore.ru/help/developers/monetization/sandbox/testing-sdk-pay)
- [Включение тестового режима](https://www.rustore.ru/help/developers/monetization/sandbox/enable-test-mode) 
- [Работа без приложения RuStore](https://www.rustore.ru/help/developers/monetization/without-rustore-app#vk-id) 

## 📋 Требования

- **Android**: API Level 21+ (Android 5.0+)
- **RuStore**: Должен быть установлен на устройстве
- **Cordova**: 9.0.0+
- **cordova-android**: 8.0.0+

