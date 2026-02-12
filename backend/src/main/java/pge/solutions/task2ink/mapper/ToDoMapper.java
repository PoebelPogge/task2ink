package pge.solutions.task2ink.mapper;

import net.fortuna.ical4j.model.component.VToDo;
import org.mapstruct.Mapper;
import pge.solutions.task2ink.dto.ApiToDo;
import pge.solutions.task2ink.dto.PrintableToDo;

import java.time.ZoneId;

@Mapper(componentModel = "spring")
public abstract class ToDoMapper {

    public ApiToDo toApi(VToDo toDo){
        if(null == toDo) return null;
        return new ApiToDo(
                toDo.getUid().getValue(),
                toDo.getSummary().getValue(),
                toDo.getDescription() != null ? toDo.getDescription().getValue() : "",
                toDo.getDue() != null ? toDo.getDue().getDate().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime() : null
        );
    }

    public PrintableToDo toPrint(VToDo todo, String listName){
        if(null == todo) return null;
        return new PrintableToDo(
                todo.getUid().getValue(),
                listName,
                todo.getSummary().getValue(),
                todo.getDescription() != null ? todo.getDescription().getValue() : "",
                todo.getDue() != null ? todo.getDue().getDate().toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime() : null
        );
    }
}
