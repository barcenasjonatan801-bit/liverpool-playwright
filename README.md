# Liverpool Playwright Challenge

[![Liverpool E2E Tests](https://github.com/barcenasjonatan801-bit/liverpool-playwright/actions/workflows/test.yml/badge.svg)](https://github.com/barcenasjonatan801-bit/liverpool-playwright/actions/workflows/test.yml)

Framework de automatización E2E para Liverpool desarrollado con Java 17, Playwright, JUnit 5, Maven, axe-core y Allure Report.

## Flujo automatizado

La prueba principal:

1. Abre Liverpool.
2. Busca `playstation 5`.
3. Aplica el filtro de color White, mostrado como `Blanco`.
4. Ordena los resultados de menor a mayor precio.
5. Extrae e imprime el nombre y precio de los primeros cinco productos.
6. Intercepta `/web-bff/product/search`.
7. Compara los productos de la UI contra la respuesta.
8. Exige al menos tres coincidencias.
9. Registra productos ausentes y diferencias de nombre o precio.

La misma prueba está parametrizada mediante JUnit y también se ejecuta para:

- `xbox series x`
- `nintendo switch`

## Bonus implementados

### Data-driven testing

Los términos de búsqueda se definen mediante `@CsvSource`, permitiendo agregar escenarios sin duplicar el código de la prueba.

### Accesibilidad

`LiverpoolAccessibilityTest` ejecuta un análisis con axe-core sobre la página de resultados utilizando reglas WCAG 2.0 y 2.1 de niveles A y AA.

El resultado genera dos evidencias en Allure:

- Resumen del análisis.
- Resultado completo en JSON.

Las violaciones del sitio se reportan, pero no bloquean el pipeline porque Liverpool es un sistema externo. La prueba sí falla si axe-core no puede completar el análisis o no devuelve resultados válidos.

## Tecnologías

- Java 17
- Maven
- Playwright para Java
- JUnit 5
- Jackson
- axe-core
- Allure Report
- GitHub Actions

## Requisitos

- JDK 17.
- Maven 3.9 o superior.
- Git.

Verificación:

```powershell
java -version
mvn -version
git --version
```

## Instalación

Clonar el repositorio:

```powershell
git clone https://github.com/barcenasjonatan801-bit/liverpool-playwright
cd liverpool-playwright
```

Instalar Firefox para Playwright:

```powershell
mvn exec:java "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.args=install firefox"
```

Las demás dependencias se descargan automáticamente mediante Maven.

## Ejecución

### Headless

Las pruebas se ejecutan en modo headless de forma predeterminada:

```powershell
mvn clean test
```

También puede indicarse explícitamente:

```powershell
mvn clean test "-Dheadless=true" "-Dbrowser=firefox"
```

### Headed

Para observar la ejecución:

```powershell
mvn clean test "-Dheadless=false" "-Dbrowser=firefox"
```

### Flujo E2E y validación de servicio

Ejecuta los escenarios data-driven:

```powershell
mvn "-Dtest=LiverpoolSearchTest" test
```

### Accesibilidad

Ejecuta únicamente axe-core:

```powershell
mvn "-Dtest=LiverpoolAccessibilityTest" test
```

### Smoke test

```powershell
mvn "-Dtest=LiverpoolSmokeTest" test
```

## Configuración

| Propiedad | Valor predeterminado | Descripción |
|---|---|---|
| `headless` | `true` | Ejecuta el navegador sin interfaz gráfica |
| `browser` | `firefox` | Navegador utilizado por Playwright |
| `baseUrl` | `https://www.liverpool.com.mx/` | URL inicial de la aplicación |

Firefox es el navegador predeterminado porque Chromium headless fue rechazado por el WAF durante el desarrollo. No se utilizaron mecanismos para evadir los controles de seguridad del sitio.

## Reporte HTML

Después de ejecutar las pruebas:

```powershell
mvn allure:report
```

El reporte se genera en:

```text
target/site/allure-maven-plugin/index.html
```

Para abrirlo en Windows:

```powershell
start target\site\allure-maven-plugin\index.html
```

También puede utilizarse:

```powershell
mvn allure:serve
```

Los fallos generan automáticamente una captura de pantalla y la adjuntan al caso correspondiente. Esta funcionalidad se implementa mediante una extensión de JUnit, no mediante capturas manuales dentro de las pruebas.

## Diseño del framework

```text
src/test/java/com/jonatan/challenge
├── base
│   └── BaseTest.java
├── model
│   └── Product.java
├── network
│   ├── SearchResponseCapture.java
│   └── SearchResponseParser.java
├── pages
│   ├── LiverpoolHomePage.java
│   └── SearchResultsPage.java
├── reporting
│   └── ScreenshotOnFailureExtension.java
├── validation
│   ├── ProductValidator.java
│   └── ValidationResult.java
├── LiverpoolAccessibilityTest.java
├── LiverpoolSearchTest.java
└── LiverpoolSmokeTest.java
```

Responsabilidades:

- `BaseTest`: ciclo de vida de Playwright y configuración del navegador.
- `pages`: interacciones con la interfaz mediante Page Object Model.
- `network`: captura y transformación de la respuesta del frontend.
- `validation`: comparación independiente entre productos UI y API.
- `reporting`: evidencia automática en caso de fallo.
- `LiverpoolSearchTest`: flujo E2E, data-driven y validación UI/API.
- `LiverpoolAccessibilityTest`: análisis WCAG y evidencias de axe-core.

Los productos se relacionan mediante su ID. Los nombres se normalizan antes de compararse y los precios se procesan con `BigDecimal` para evitar errores de precisión.

## Integración continua

El workflow se encuentra en:

```text
.github/workflows/test.yml
```

En cada push o pull request:

1. Configura Java 17.
2. Instala Firefox y sus dependencias.
3. Ejecuta las pruebas en modo headless.
4. Genera el reporte Allure.
5. Publica `allure-html-report` como artefacto.

## Estrategia de pruebas

Las decisiones sobre cobertura, CAPTCHA, mitigación de flakiness y escalabilidad en CI están documentadas en [TEST_STRATEGY.md](TEST_STRATEGY.md).