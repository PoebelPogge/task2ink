package pge.solutions.task2ink.mapper;

import net.fortuna.ical4j.model.component.VToDo;
import org.mapstruct.Mapper;
import pge.solutions.task2ink.dto.ApiToDo;

import java.time.ZoneId;

@Mapper(componentModel = "spring")
public abstract class ToDoMapper {

    public ApiToDo toApi(VToDo toDo){
        if(null == toDo) return null;
        return new ApiToDo(
                toDo.getUid().getValue(),
                toDo.getSummary().getValue(),
                toDo.getDescription() != null ? toDo.getDescription().getValue() : "",
                toDo.getDue().getDate().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime()
        );
    }
}
