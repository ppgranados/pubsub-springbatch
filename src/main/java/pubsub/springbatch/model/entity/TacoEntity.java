package pubsub.springbatch.model.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "tacos")
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(value = AccessLevel.PACKAGE)
@Builder
public class TacoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "taco_id")
    private Long id;

    private String type;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Override
    public String toString() {
        return "TacoEntity{" +
                "id=" + id +
                ", type='" + type + '\'' +
                '}';
    }
}
