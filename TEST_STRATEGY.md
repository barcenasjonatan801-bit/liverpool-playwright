# Estrategia de pruebas

## Alcance y enfoque

Esta suite valida la búsqueda de Liverpool en dos capas: los productos visibles en la interfaz y la respuesta de `/web-bff/product/search` consumida por el frontend. La capa UI comprueba que el usuario puede buscar, aplicar el filtro de color blanco, ordenar por precio ascendente y visualizar la información de los productos. La capa de servicio verifica que los productos renderizados correspondan con la respuesta interceptada.

### 1. ¿Qué no automatizaría en este flujo y por qué?

No validaría nombres, precios, cantidades de resultados ni productos específicos. Estos datos dependen del inventario, promociones, vendedores externos, personalización y reglas comerciales. Las validaciones exactas generarían fallos sin representar necesariamente un defecto. En su lugar, la prueba valida el comportamiento: existen cinco productos, contienen los campos requeridos, sus precios están ordenados y aparecen en la respuesta interceptada.

Tampoco incluiría checkout, pagos, recomendaciones ni servicios de terceros porque están fuera del alcance. La disponibilidad de esos componentes no debería afectar la validación de la búsqueda. Los banners y aspectos visuales subjetivos se revisarían manualmente o mediante pruebas visuales controladas.

### 2. ¿Cómo manejaría un CAPTCHA?

La suite nunca intentaría resolver o evadir el CAPTCHA. En un ambiente controlado solicitaría un mecanismo aprobado, como una bandera de configuración, identidad de CI autorizada o test hook que desactive el desafío sin alterar el resto del flujo. La activación del CAPTCHA se probaría por separado en la capa de seguridad o integración.

Si producción fuera el único ambiente disponible, la prueba terminaría con un resultado claramente clasificado como bloqueado y se mantendría una validación manual mínima. No utilizaría servicios de resolución, plugins furtivos, suplantación de huellas del navegador ni técnicas para evadir controles.

### 3. ¿Qué riesgos de flakiness existen y cómo se mitigaron?

Los principales riesgos son la hidratación asíncrona, cambios de inventario y precios, respuestas lentas, localización, modificaciones del DOM y comportamiento del WAF en navegadores headless. Se mitigan mediante Page Objects, contextos de navegador independientes, selectores estables basados en `data-testid` y roles accesibles, además de esperas por estados observables en lugar de pausas fijas.

Firefox es el navegador headless predeterminado porque Chromium fue rechazado por el WAF durante el desarrollo. Los productos se relacionan mediante su ID estable y los nombres normalizados y precios decimales se comparan por separado. El mínimo de tres coincidencias permite tolerar cambios legítimos del catálogo durante la ejecución, mientras se registran todos los productos ausentes y diferencias de nombre o precio. Los fallos generan screenshots automáticamente.

### 4. ¿Qué cambiaría al integrar esta prueba con más de 50 suites?

Clasificaría esta prueba como un smoke test E2E externo y la separaría de las pruebas unitarias, de componentes y de API. Los pull requests ejecutarían primero las pruebas rápidas y deterministas; los E2E externos se ejecutarían en una etapa con paralelismo limitado o mediante una programación periódica para evitar sobrecargar Liverpool y respetar límites de peticiones.

Agregaría etiquetas, reintentos limitados solamente para errores transitorios documentados, métricas de duración, trazas en reintentos, caché de dependencias y seguimiento histórico de flakiness. La cobertura de navegadores utilizaría una matriz reducida. Los errores de WAF, conectividad o ambiente se clasificarían por separado de los defectos funcionales. Antes de aumentar el paralelismo, preferiría un ambiente controlado y datos de prueba estables.
