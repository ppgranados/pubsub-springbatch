package pubsub.springbatch.model.entity;


import lombok.*;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Builder
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_id")
    private Long id;

    private Double price;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TacoEntity> tacos;

    public void addTacos(final List<TacoEntity> tacos) {
        this.setTacos(tacos);
    }

    @Override
    public String toString() {
        return "OrderEntity{" +
                "id=" + id +
                ", price=" + price +
                ", customer=" + customer +
                ", tacos=" + tacos +
                '}';
    }
}
