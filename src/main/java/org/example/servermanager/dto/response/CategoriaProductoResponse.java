package org.example.servermanager.dto.response;

import org.example.servermanager.entity.CategoriaProducto;

public record CategoriaProductoResponse(
        Long id,
        Long empresaId,
        String nombre,
        String descripcion,
        Integer orden,
        Boolean activo,
        Integer cantidadProductos
) {
    public static CategoriaProductoResponse fromEntity(CategoriaProducto categoria) {
        return new CategoriaProductoResponse(
                categoria.getId(),
                categoria.getEmpresa().getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getOrden(),
                categoria.getActivo(),
                categoria.getProductos() != null ? categoria.getProductos().size() : 0
        );
    }
}
