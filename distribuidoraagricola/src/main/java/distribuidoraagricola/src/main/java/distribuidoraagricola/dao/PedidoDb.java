package distribuidoraagricola.dao;

import distribuidoraagricola.modelo.DetallePedido;
import distribuidoraagricola.modelo.EstadoPedido;
import distribuidoraagricola.modelo.Pedido;
import distribuidoraagricola.persistencia.PersistenciaUtil;

import jakarta.persistence.EntityManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para Pedidos usando JPA/EntityManager.
 *
 * insertarPedido/insertarDetallePedido reciben el EntityManager del
 * llamador porque PedidoService.registrarPedido() necesita guardar
 * el pedido y todas sus lineas de detalle en una sola transaccion.
 * actualizarEstado tiene dos versiones: la simple (usada por
 * confirmarPedido/cancelarPedido, que solo cambian un campo) y la
 * que recibe EntityManager (usada por despacharPedido, que cambia
 * el estado junto con el inventario y la factura en una transaccion).
 */
public class PedidoDb {

    public int insertarPedido(EntityManager em, Pedido p) {
        if (p.getFechaPedido() == null) {
            p.setFechaPedido(new Timestamp(System.currentTimeMillis()));
        }
        em.persist(p);
        em.flush(); // para que p.getIdPedido() quede disponible de inmediato
        return p.getIdPedido();
    }

    public boolean insertarDetallePedido(EntityManager em, int idPedido, DetallePedido d) {
        d.setIdPedido(idPedido);
        em.persist(d);
        return true;
    }

    public boolean actualizarEstado(int idPedido, EstadoPedido nuevoEstado) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            em.getTransaction().begin();
            boolean ok = actualizarEstado(em, idPedido, nuevoEstado);
            em.getTransaction().commit();
            return ok;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.out.println("Error al actualizar el estado del pedido: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    /** Sobrecarga que participa en la transaccion del llamador (ver nota de la clase). */
    public boolean actualizarEstado(EntityManager em, int idPedido, EstadoPedido nuevoEstado) {
        Pedido p = em.find(Pedido.class, idPedido);
        if (p == null) {
            return false;
        }
        p.setEstado(nuevoEstado);
        return true;
    }

    public Pedido buscarPorId(int idPedido) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            List<Object[]> filas = em.createQuery(
                    "SELECT p, c.nombre FROM Pedido p, Cliente c "
                    + "WHERE p.idCliente = c.idCliente AND p.idPedido = :idPedido",
                    Object[].class)
                    .setParameter("idPedido", idPedido)
                    .getResultList();
            if (filas.isEmpty()) {
                return null;
            }
            Pedido p = (Pedido) filas.get(0)[0];
            p.setNombreCliente((String) filas.get(0)[1]);
            p.setDetalle(listarDetalle(idPedido));
            return p;
        } catch (RuntimeException e) {
            System.out.println("Error al buscar el pedido: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public List<DetallePedido> listarDetalle(int idPedido) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            List<Object[]> filas = em.createQuery(
                    "SELECT d, p.nombre FROM DetallePedido d, ProductoAgricola p "
                    + "WHERE d.idProducto = p.idProducto AND d.idPedido = :idPedido "
                    + "ORDER BY d.idDetallePedido",
                    Object[].class)
                    .setParameter("idPedido", idPedido)
                    .getResultList();
            List<DetallePedido> lista = new ArrayList<>();
            for (Object[] fila : filas) {
                DetallePedido d = (DetallePedido) fila[0];
                d.setNombreProducto((String) fila[1]);
                lista.add(d);
            }
            return lista;
        } catch (RuntimeException e) {
            System.out.println("Error al listar el detalle del pedido: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<Pedido> listarTodos() {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            return listarConNombreCliente(em, "SELECT p, c.nombre FROM Pedido p, Cliente c "
                    + "WHERE p.idCliente = c.idCliente ORDER BY p.idPedido", -1);
        } catch (RuntimeException e) {
            System.out.println("Error al listar pedidos: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<Pedido> listarPorCliente(int idCliente) {
        EntityManager em = PersistenciaUtil.crearEntityManager();
        try {
            return listarConNombreCliente(em, "SELECT p, c.nombre FROM Pedido p, Cliente c "
                    + "WHERE p.idCliente = c.idCliente AND p.idCliente = :idCliente ORDER BY p.idPedido", idCliente);
        } catch (RuntimeException e) {
            System.out.println("Error al listar pedidos del cliente: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    private List<Pedido> listarConNombreCliente(EntityManager em, String jpql, int idClienteFiltro) {
        jakarta.persistence.TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
        if (idClienteFiltro >= 0) {
            query.setParameter("idCliente", idClienteFiltro);
        }
        List<Pedido> lista = new ArrayList<>();
        for (Object[] fila : query.getResultList()) {
            Pedido p = (Pedido) fila[0];
            p.setNombreCliente((String) fila[1]);
            lista.add(p);
        }
        return lista;
    }
}
