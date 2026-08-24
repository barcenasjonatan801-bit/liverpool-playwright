# Liverpool Playwright Challenge

[![Liverpool E2E Tests](https://github.com/barcenasjonatan801-bit/liverpool-playwright/actions/workflows/test.yml/badge.svg)](https://github.com/barcenasjonatan801-bit/liverpool-playwright/actions/workflows/test.yml)

Framework de automatización E2E para validar el flujo de búsqueda de productos en [Liverpool](https://www.liverpool.com.mx/), desarrollado con Java 17, Playwright, JUnit 5, Maven y Allure Report.

## Flujo automatizado

La prueba principal realiza las siguientes acciones:

1. Abre Liverpool.
2. Busca `playstation 5`.
3. Aplica el filtro de color White, mostrado como `Blanco` en la interfaz en español.
4. Ordena los resultados de menor a mayor precio.
5. Extrae e imprime el nombre y precio de los primeros cinco productos.
6. Intercepta la respuesta de `/web-bff/product/search`.
7. Compara los productos de la UI contra la respuesta del servicio.
8. Exige al menos tres coincidencias y registra diferencias de nombre o precio.

## Tecnologías

* Java 17
* Maven
* Playwright para Java
* JUnit 5
* Jackson
* Allure Report
* GitHub Actions

## Requisitos

Antes de ejecutar el proyecto se necesita:

* JDK 17.
* Maven 3.9 o superior.
* Git.

Verificación:

```powershell
java -version
mvn -version
git --version
```

## Instalación

Clona el repositorio y entra en el proyecto:

```powershell
git clone https://github.com/barcenasjonatan801-bit/liverpool-playwright
cd liverpool-playwright
```

Instala Firefox para Playwright:

```powershell
mvn exec:java "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.args=install firefox"
```

## Ejecución

### Headless

Las pruebas se ejecutan en modo headless de manera predeterminada:

```powershell
mvn clean test
```

También puede indicarse explícitamente:

```powershell
mvn clean test "-Dheadless=true" "-Dbrowser=firefox"
```

### Headed

Para observar la ejecución en el navegador:

```powershell
mvn clean test "-Dheadless=false" "-Dbrowser=firefox"
```

### Prueba principal

Para ejecutar únicamente el flujo requerido:

```powershell
mvn "-Dtest=LiverpoolSearchTest" test
```

## Configuración

El framework acepta las siguientes propiedades de Maven:

| Propiedad  | Valor predeterminado            | Descripción                               |
| ---------- | ------------------------------- | ----------------------------------------- |
| `headless` | `true`                          | Ejecuta el navegador sin interfaz gráfica |
| `browser`  | `firefox`                       | Navegador utilizado por Playwright        |
| `baseUrl`  | `https://www.liverpool.com.mx/` | URL inicial de la aplicación              |

Firefox se utiliza como navegador predeterminado porque, durante el desarrollo, Chromium headless fue rechazado por el WAF del sitio. No se utilizaron técnicas para evadir los controles de seguridad.

## Reporte HTML

Después de ejecutar las pruebas, genera el reporte estático:

```powershell
mvn allure:report
```

El archivo principal se genera en:

```text
target/site/allure-maven-plugin/index.html
```

Para generar y abrir el reporte mediante un servidor local:

```powershell
mvn allure:serve
```

Los fallos producen automáticamente un screenshot de la página y lo adjuntan al caso correspondiente en Allure. La captura está implementada como una extensión de JUnit y no dentro de los métodos de prueba.

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
├── LiverpoolSearchTest.java
└── LiverpoolSmokeTest.java
```

Responsabilidades principales:

* `BaseTest`: ciclo de vida de Playwright y configuración del navegador.
* `pages`: interacción con la interfaz mediante Page Object Model.
* `network`: captura y transformación de la respuesta utilizada por el frontend.
* `validation`: comparación independiente entre productos UI y API.
* `reporting`: evidencia automática en caso de fallo.
* `LiverpoolSearchTest`: orquestación y aserciones del flujo requerido.

Los productos se relacionan mediante su ID estable. Los nombres se normalizan antes de compararse y los precios se procesan con `BigDecimal` para evitar errores de precisión.

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
5. Publica `allure-html-report` como artefacto descargable.

## Estrategia de pruebas

Las decisiones de cobertura, manejo de CAPTCHA, mitigación de flakiness y escalabilidad en CI están documentadas en [TEST_STRATEGY.md](TEST_STRATEGY.md).
