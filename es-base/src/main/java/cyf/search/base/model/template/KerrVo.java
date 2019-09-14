package cyf.search.base.model.template;

//import cyf.search.dao.model.Employees;
import lombok.Data;
import org.springframework.data.annotation.Id;
//import org.springframework.data.elasticsearch.annotations.Document;

/**
 * @author Cheng Yufei
 * @create 2017-12-23 15:52
 **/
@Data
//@Document(indexName = "miranda",type = "kerr",replicas = -1)
public class KerrVo {

    @Id
    private Integer id;

    private Integer votes;

    private String title;

    private String city;

    private String features;

    private float price;

    private String color;

    private Name name;


//    @Field(type = FieldType.Date )
    private String publishtime;

    //private Employees employees;


}
