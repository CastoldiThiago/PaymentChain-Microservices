# Style Guide - Payment Chain Frontend

Este documento describe el sistema de diseño implementado en la aplicación.

## 🎨 Sistema de Colores

### Colores Principales

```css
--primary-color: #4f46e5 /* Indigo 600 */ --primary-hover: #4338ca
  /* Indigo 700 */ --secondary-color: #64748b /* Slate 500 */
  --success-color: #10b981 /* Green 500 */ --danger-color: #ef4444 /* Red 500 */
  --warning-color: #f59e0b /* Amber 500 */;
```

### Colores de Superficie

```css
--background: #f8fafc /* Slate 50 */ --surface: #ffffff /* White */
  --text-primary: #1e293b /* Slate 800 */ --text-secondary: #64748b
  /* Slate 500 */ --border-color: #e2e8f0 /* Slate 200 */;
```

## 📏 Espaciado

- **Base**: 8px
- **Pequeño**: 4px
- **Mediano**: 16px
- **Grande**: 24px
- **Extra Grande**: 32px

## 🔤 Tipografía

### Fuentes

```css
font-family:
  -apple-system, BlinkMacSystemFont, "Segoe UI", "Roboto", "Oxygen", "Ubuntu",
  "Cantarell", "Fira Sans", "Droid Sans", "Helvetica Neue", sans-serif;
```

### Tamaños

- **Título de Página**: 2rem (32px)
- **Título de Card**: 1.25rem (20px)
- **Texto Normal**: 0.875rem (14px)
- **Texto Pequeño**: 0.75rem (12px)

## 🧱 Componentes

### Navegación

```html
<nav className="nav-bar">
  <div className="nav-content">
    <Link to="/path" className="nav-link">Texto</Link>
  </div>
</nav>
```

### Contenedor de Página

```html
<div className="page-container">
  <div className="page-header">
    <h1 className="page-title">Título</h1>
    <p className="page-subtitle">Subtítulo</p>
  </div>
  <!-- Contenido -->
</div>
```

### Cards

```html
<div className="card">
  <h3 className="card-title">Título</h3>
  <!-- Contenido del card -->
</div>
```

### Formularios

```html
<form className="form-grid">
  <div className="form-group">
    <label className="form-label">Etiqueta</label>
    <input className="form-input" placeholder="..." />
  </div>
  <div className="form-actions">
    <button className="btn btn-primary">Enviar</button>
  </div>
</form>
```

### Botones

```html
<!-- Primario -->
<button className="btn btn-primary">Acción Principal</button>

<!-- Secundario -->
<button className="btn btn-secondary">Acción Secundaria</button>

<!-- Peligro -->
<button className="btn btn-danger">Eliminar</button>

<!-- Outline -->
<button className="btn btn-outline">Cancelar</button>

<!-- Pequeño -->
<button className="btn btn-primary btn-sm">Pequeño</button>
```

### Tablas

```html
<div className="table-container">
  <table className="data-table">
    <thead>
      <tr>
        <th>Columna 1</th>
        <th>Columna 2</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>Dato 1</td>
        <td>Dato 2</td>
      </tr>
    </tbody>
  </table>
</div>
```

### Badges

```html
<span className="badge badge-success">Activo</span>
<span className="badge badge-warning">Pendiente</span>
<span className="badge badge-danger">Error</span>
<span className="badge badge-info">Info</span>
```

### Filtros

```html
<div className="filter-section">
  <div className="filter-group">
    <label className="form-label">Filtro</label>
    <input className="form-input" placeholder="..." />
  </div>
  <button className="btn btn-primary">Aplicar</button>
  <button className="btn btn-outline">Limpiar</button>
</div>
```

### Paginación

```html
<div className="pagination">
  <div className="pagination-info">
    Mostrando página <span className="page-number">1</span> de
    <span className="page-number">10</span>
  </div>
  <div className="pagination-controls">
    <button className="btn btn-outline btn-sm">Anterior</button>
    <button className="btn btn-outline btn-sm">Siguiente</button>
  </div>
</div>
```

### Estadísticas

```html
<div className="stats-grid">
  <div className="stat-card">
    <div className="stat-label">Etiqueta</div>
    <div className="stat-value">100</div>
  </div>
</div>
```

## 📱 Responsividad

### Breakpoints

- **Mobile**: < 768px
- **Desktop**: ≥ 768px

### Grid Responsivo

```html
<!-- 2 columnas en desktop, 1 en mobile -->
<div className="grid-2">
  <div>Contenido 1</div>
  <div>Contenido 2</div>
</div>
```

## ♿ Accesibilidad

### Consideraciones

- Contraste de color mínimo WCAG AA
- Labels para todos los inputs
- Estados focus visibles
- Textos alt en imágenes (cuando aplique)
- Navegación por teclado
- Estados disabled claros

## 🎯 Buenas Prácticas

1. **Consistencia**: Usa siempre las clases del sistema
2. **Semántica**: HTML semántico apropiado
3. **Espaciado**: Usa el sistema de espaciado definido
4. **Colores**: Usa las variables CSS definidas
5. **Responsive**: Mobile-first approach
6. **Accesibilidad**: Siempre considera usuarios con discapacidades

## 🔄 Actualizaciones

Para modificar el sistema de diseño, edita:

- `/src/styles/global.css` - Variables y estilos globales
