from typing import List, Any, Annotated
from sqlmodel import SQLModel, Field, JSON
from sqlalchemy import Column
from pydantic import validator, field_validator, BeforeValidator


def _validate_tags(v: Any):
    if isinstance(v, str):
        return [t.strip().lower() for t in v.split(",") if t.strip()]
    return [str(t).lower() for t in v] if v else []


class TaggableMixin(SQLModel):
    """
    Mixin para añadir soporte de etiquetas (tags) a cualquier modelo.
    Se almacena como un JSONB.
    """

    tags: Annotated[list[str], BeforeValidator(_validate_tags)] = Field(
        default_factory=list,
        sa_column=Column(JSON),
        description="Lista de etiquetas para categorización flexible"
    )
