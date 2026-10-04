package distribuidoraagricola.dao;

import distribuidoraagricola.modelo.Compra;
import distribuidoraagricola.modelo.DetalleCompra;
import distribuidoraagricola.persistencia.PersistenciaUtil;

import jakarta.persistence.EntityManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para Compras usando JPA/EntityManager.
 *
 * insertarCompra/insertarDetalleCompra reciben el EntityManager del
 * llamador a proposito: CompraService.registrarCompra() necesita que
 * la compra, su detalle y el aumento de existencia de cada producto
 * ocurran en UNA sola transaccion (si algo falla, se revierte todo).
 * Por eso estos dos metodos no manejan su propio EntityManager ni
 * transaccion, a diferencia de listarTodas()/listarDetalle().
 */
public class CompraDb {

    public int insertarCompra(EntityManager em, Compra c) {
        if (c.getFechaCompra() == null) {
            c.setFechaCompra(new Timestamp(System.currentTimeMillis()));
        }
        em.persist(c);
        em.flush(); // para que c.getIdCompra() quede disponible de inmediato
        return c.getIdCompra();
    }

    public boolean insertarDetalleCompra(EntityManager em, int idCompra, DetalleCompra d) {
        d.setIdCompra(idCompra);
        em.persist(d);
        return true;
    }

    public List<Compra> listarTodas() {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            List<Object[]> filas = em.createQuery(
                    "SELECT c, pr.nombre FROM Compra c, Proveedor pr "
                    + "WHERE c.idProveedor = pr.idProveedor ORDER BY c.idCompra",
                    Object[].class).getResultList();
            List<Compra> lista = new ArrayList<>();
            for (Object[] fila : filas) {
                Compra c = (Compra) fila[0];
                c.setNombreProveedor((String) fila[1]);
                lista.add(c);
            }
            return lista;
        } catch (RuntimeException e) {
            System.out.println("Error al listar compras: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<DetalleCompra> listarDetalle(int idCompra) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            List<Object[]> filas = em.createQuery(
                    "SELECT d, p.nombre FROM DetalleCompra d, ProductoAgricola p "
                    + "WHERE d.idProducto = p.idProducto AND d.idCompra = :idCompra "
                    + "ORDER BY d.idDetalleCompra",
                    Object[].class)
                    .setParameter("idCompra", idCompra)
                    .getResultList();
            List<DetalleCompra> lista = new ArrayList<>();
            for (Object[] fila : filas) {
                DetalleCompra d = (DetalleCompra) fila[0];
                d.setNombreProducto((String) fila[1]);
                lista.add(d);
            }
            return lista;
        } catch (RuntimeException e) {
            System.out.println("Error al listar el detalle de la compra: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
}
