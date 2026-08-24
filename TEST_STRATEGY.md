# Estrategia de pruebas

## Alcance

La suite valida el flujo de búsqueda de Liverpool desde dos capas. La UI comprueba la búsqueda, el filtro de color blanco, el orden ascendente y los primeros cinco productos. La capa de servicio intercepta `/web-bff/product/search` y verifica que los productos renderizados correspondan con los datos consumidos por el frontend. Como cobertura adicional, axe-core analiza la accesibilidad de la página de resultados y adjunta la evidencia en Allure.

## 1. ¿Qué no automatizaría y por qué?

No validaría productos, nombres, precios, inventario o cantidades específicas porque cambian por promociones, disponibilidad, vendedores y reglas comerciales. Una aserción rígida podría fallar aunque la aplicación funcione correctamente. En su lugar, valido la estructura, el orden de precios y la correspondencia entre UI y servicio mediante identificadores de producto.

Tampoco incluiría checkout, pagos, recomendaciones, banners ni servicios de terceros porque están fuera del alcance y aumentarían el costo y la inestabilidad sin aportar valor al objetivo evaluado. Los aspectos visuales subjetivos y los problemas de accesibilidad que requieren juicio humano se complementarían con revisión manual.

## 2. ¿Cómo manejaría un CAPTCHA?

Nunca intentaría resolverlo o evadirlo. En un ambiente controlado solicitaría un mecanismo aprobado, como una identidad de CI autorizada, allowlist o test hook para deshabilitar únicamente el desafío. El comportamiento del CAPTCHA se probaría por separado como control de seguridad.

Si producción fuera el único ambiente disponible, clasificaría la ejecución como bloqueada por el ambiente y mantendría una validación manual mínima. No utilizaría servicios externos, modificación de huellas del navegador ni plugins furtivos.

## 3. ¿Qué riesgos de flakiness existen y cómo se mitigaron?

Los riesgos principales son la hidratación asíncrona, el autocompletado, cambios de catálogo, respuestas lentas, modificaciones del DOM y bloqueos del WAF. Se mitigaron con Page Objects, locators por roles, labels y `data-testid`, y esperas basadas en estados observables, sin pausas fijas. Si una sugerencia no aparece, la búsqueda continúa mediante Enter, que representa una interacción válida.

Cada prueba utiliza un contexto independiente. Los productos se comparan por ID; nombres y precios se validan separadamente con normalización y `BigDecimal`. El umbral de tres coincidencias tolera cambios legítimos mientras registra todas las discrepancias. Firefox es el navegador predeterminado porque Chromium headless fue rechazado por el WAF. Los listeners de red se eliminan antes de cerrar el contexto y los fallos adjuntan screenshots automáticamente en Allure.

## 4. ¿Qué cambiaría para un CI con más de 50 suites?

Clasificaría esta suite como E2E externo. Los pull requests ejecutarían primero pruebas unitarias, de componentes y API; el flujo real se ejecutaría como smoke controlado o periódicamente, con paralelismo limitado para no sobrecargar Liverpool.

Agregaría etiquetas, caché de dependencias, métricas de duración y flakiness, trazas en reintentos y clasificación separada de errores funcionales, WAF y conectividad. Los reintentos se limitarían a fallos transitorios conocidos. Mantendría una sola prueba real como canary y trasladaría más escenarios a pruebas de servicio deterministas. La accesibilidad usaría una línea base acordada antes de convertirse en un quality gate.