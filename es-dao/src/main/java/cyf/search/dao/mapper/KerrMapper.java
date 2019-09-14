package cyf.search.dao.mapper;

import cyf.search.dao.model.Kerr;
import cyf.search.dao.model.KerrExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface KerrMapper {
    long countByExample(KerrExample example);

    int deleteByExample(KerrExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Kerr record);

    int insertSelective(Kerr record);

    int selfPlusMinus(@Param("columnName") String columnName, @Param("operator") String operator, @Param("count") int count, @Param("example") Object example);

    int selfPlusMinusByPrimaryKey(@Param("columnName") String columnName, @Param("operator") String operator, @Param("count") int count, @Param("id") int id);

    List<Kerr> selectByExample(KerrExample example);

    Kerr selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Kerr record, @Param("example") KerrExample example);

    int updateByExample(@Param("record") Kerr record, @Param("example") KerrExample example);

    int updateByPrimaryKeySelective(Kerr record);

    int updateByPrimaryKey(Kerr record);
}