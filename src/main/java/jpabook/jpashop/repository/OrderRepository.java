package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;
    private final QOrder order = QOrder.order;
    private final QMember member = QMember.member;

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    public List<Order> findAllByQuerydsl(OrderSearch orderSearch){
        JPAQueryFactory query = new JPAQueryFactory(em);
        List<Order> list =
                query.selectFrom(order)
                        .join(order.member, member)
                        .where(eqOrderStatus(orderSearch.getOrderStatus())
                                ,eqMemberName(orderSearch.getMemberName()))
                        .limit(1000)
                        .fetch();

        return list;
    }

    private BooleanExpression eqOrderStatus(OrderStatus orderStatus) {
        if (StringUtils.isEmpty(orderStatus)) {
            return null;
        }
        return order.status.eq(orderStatus);
    }

    private BooleanExpression eqMemberName(String memberName) {
        if (StringUtils.isEmpty(memberName)) {
            return null;
        }
        return member.name.like(memberName);
    }

    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }    //회원 이름 검색
         if (StringUtils.hasText(orderSearch.getMemberName())) {
             Predicate name =  cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
             criteria.add(name);
        }
         cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
         TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
         return query.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }



}
