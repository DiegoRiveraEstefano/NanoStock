from typing import TypeVar, Generic, Any

from pydantic import BaseModel, Field

T = TypeVar("T")

class BaseResultError(BaseModel):
    error: BaseException
    message: str
    data: dict[str, Any] = Field(default_factory=dict)


class Success(BaseModel, Generic[T]):
    value: T
    is_success: bool = True

class Failure(BaseModel):
    error: BaseResultError
    is_success: bool = False


Result = Success[T] | Failure
