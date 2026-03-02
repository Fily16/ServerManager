package org.example.servermanager.dto.response;

import org.example.servermanager.entity.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoResponse(
        Long id,
        Long empresaId,
        Long categoriaId,
        String categoriaNombre,
        String nombre,
        String descripcion,
        BigDecimal precio,
        BigDecimal precioOferta,
        String imagenUrl,
        Boolean tieneStock,
        Integer stockActual,
        Boolean esDestacado,
        String tags,
        Boolean activo,
        LocalDateTime fechaCreacion
) {
    public static ProductoResponse fromEntity(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getEmpresa().getId(),
                producto.getCategoria() != null ? producto.getCategoria().getId() : null,
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getPrecioOferta(),
                producto.getImagenUrl(),
                producto.getTieneStock(),
                producto.getStockActual(),
                producto.getEsDestacado(),
                producto.getTags(),
                producto.getActivo(),
                producto.getFechaCreacion()
        );
    }
}
