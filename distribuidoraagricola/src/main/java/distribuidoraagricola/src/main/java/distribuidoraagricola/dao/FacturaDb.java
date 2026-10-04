package distribuidoraagricola.dao;

import distribuidoraagricola.modelo.Factura;
import distribuidoraagricola.persistencia.PersistenciaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para Facturas usando JPA/EntityManager.
 *
 * generar() recibe el EntityManager del llamador porque
 * PedidoService.despacharPedido() genera la factura en la misma
 * transaccion en la que descuenta el inventario y cambia el estado
 * del pedido.
 */
public class FacturaDb {

    public boolean generar(EntityManager em, int idPedido, double total) {
        Factura f = new Factura();
        f.setIdPedido(idPedido);
        f.setFechaFactura(new Timestamp(System.currentTimeMillis()));
        f.setTotal(total);
        em.persist(f);
        return true;
    }

    public List<Factura> listarTodas() {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            return mapearConNombreCliente(em,
                    "SELECT f, cl.nombre FROM Factura f, Pedido pe, Cliente cl "
                    + "WHERE f.idPedido = pe.idPedido AND pe.idCliente = cl.idCliente "
                    + "ORDER BY f.idFactura", -1);
        } catch (RuntimeException e) {
            System.out.println("Error al listar facturas: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Factura buscarPorPedido(int idPedido) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            List<Factura> lista = mapearConNombreCliente(em,
                    "SELECT f, cl.nombre FROM Factura f, Pedido pe, Cliente cl "
                    + "WHERE f.idPedido = pe.idPedido AND pe.idCliente = cl.idCliente "
                    + "AND f.idPedido = :idPedido", idPedido);
            return lista.isEmpty() ? null : lista.get(0);
        } catch (RuntimeException e) {
            System.out.println("Error al buscar factura: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public List<Factura> listarPorCliente(int idCliente) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            TypedQuery<Object[]> q = em.createQuery(
                    "SELECT f, cl.nombre FROM Factura f, Pedido pe, Cliente cl "
                    + "WHERE f.idPedido = pe.idPedido AND pe.idCliente = cl.idCliente "
                    + "AND pe.idCliente = :idCliente ORDER BY f.idFactura",
                    Object[].class);
            q.setParameter("idCliente", idCliente);
            List<Factura> lista = new ArrayList<>();
            for (Object[] fila : q.getResultList()) {
                Factura f = (Factura) fila[0];
                f.setNombreCliente((String) fila[1]);
                lista.add(f);
            }
            return lista;
        } catch (RuntimeException e) {
            System.out.println("Error al listar facturas del cliente: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    private List<Factura> mapearConNombreCliente(EntityManager em, String jpql, int idPedidoFiltro) {
        TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        if (idPedidoFiltro >= 0) {
            query.setParameter("idPedido", idPedidoFiltro);
        }
        List<Factura> lista = new ArrayList<>();
        for (Object[] fila : query.getResultList()) {
            Factura f = (Factura) fila[0];
            f.setNombreCliente((String) fila[1]);
            lista.add(f);
        }
        return lista;
    }
}
