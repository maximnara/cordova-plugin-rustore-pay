# Cordova Plugin RuStore Pay

Cordova плагин для интеграции с платежной системой RuStore. Позволяет осуществлять покупки внутри приложений, получать информацию о продуктах и управлять платежами через российский магазин приложений RuStore.

## ☕ Поддержи разработку

Если этот плагин был полезен для вашего проекта, рассмотрите возможность поддержать разработку!

[![Поддержать на Boosty](media/maximnara-donate.png)](https://boosty.to/maximnara/donate)

**[💖 Отправить донат через Boosty](https://boosty.to/maximnara/donate)**

Ваша поддержка помогает развивать проект и создавать новые полезные инструменты для сообщества!

## 📋 Содержание

- [Демонстрация](#-демонстрация)
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
- [Требования](#требования)
- [Лицензия](#лицензия)

## 🚀 Установка

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
const products = await RustorePay.getProducts({
    productIds: ["premium_subscription", "coins_100"]
});

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

### 🧾 `getPurchases()`

Получает список всех покупок пользователя.

**Возвращает:**
```typescript
Promise<{
  purchases: Array<{
    purchaseId: string;      // ID покупки
    invoiceId: string;       // ID счета
    description: string;     // Описание
    purchaseTime: number;    // Время покупки (timestamp)
    orderId: string;         // ID заказа
    amountLabel: string;     // Сумма
    currency: string;        // Валюта
    developerPayload: string; // Дополнительные данные
  }>
}>
```

**Пример:**
```javascript
const result = await RustorePay.getPurchases({});
console.log(`Найдено покупок: ${result.purchases.length}`);

result.purchases.forEach(purchase => {
    const date = new Date(purchase.purchaseTime);
    console.log(`${purchase.purchaseId}: ${purchase.amountLabel} (${date})`);
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

## 🤝 Поддержка

Если у вас возникли вопросы или проблемы:

1. Проверьте [Issues](../../issues) на GitHub
2. Создайте новый Issue с описанием проблемы
3. Приложите логи и код для воспроизведения

## 📄 Лицензия

MIT License

## ☕ Понравился проект? Поддержи разработчика!

Разработка и поддержка плагинов требует времени и усилий. Если проект оказался полезным, буду благодарен за поддержку!

<div align="center">

[![Поддержать на Boosty](media/maximnara-donate.png)](https://boosty.to/maximnara/donate)

**[💖 Отправить донат через Boosty](https://boosty.to/maximnara/donate)**

*Каждый донат мотивирует на создание новых полезных инструментов!*

</div>

---

<p align="center">
  Сделано с ❤️ для российских разработчиков
</p>