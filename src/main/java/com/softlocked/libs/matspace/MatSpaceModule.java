package com.softlocked.libs.matspace;

import com.softlocked.orbit.core.datatypes.Variable;
import com.softlocked.orbit.interpreter.function.BFunction;
import com.softlocked.orbit.interpreter.function.NativeFunction;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.java.OrbitJavaLibrary;
import com.softlocked.orbit.memory.ILocalContext;

import java.util.ArrayList;
import java.util.List;

public class MatSpaceModule implements OrbitJavaLibrary {
    @Override
    public void load(GlobalContext context) {
        // Vector addition
        context.addFunction(new NativeFunction("vector.add", List.of(Variable.Type.LIST, Variable.Type.LIST), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<Object> a = (List<Object>) args.get(0);
                List<Object> b = (List<Object>) args.get(1);
                List<Object> result = new ArrayList<>();
                for (int i = 0; i < a.size(); i++) {
                    result.add(((Number) a.get(i)).doubleValue() + ((Number) b.get(i)).doubleValue());
                }
                return result;
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<Object> a = (List<Object>) values.get(0).evaluate(context);
                        List<Object> b = (List<Object>) values.get(1).evaluate(context);
                        List<Object> result = new ArrayList<>();
                        for (int i = 0; i < a.size(); i++) {
                            result.add(((Number) a.get(i)).doubleValue() + ((Number) b.get(i)).doubleValue());
                        }
                        return result;
                    }
                }.getClass());
            }
        });

        // Vector subtraction
        context.addFunction(new NativeFunction("vector.sub", List.of(Variable.Type.LIST, Variable.Type.LIST), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<Object> a = (List<Object>) args.get(0);
                List<Object> b = (List<Object>) args.get(1);
                List<Object> result = new ArrayList<>();
                for (int i = 0; i < a.size(); i++) {
                    result.add(((Number) a.get(i)).doubleValue() - ((Number) b.get(i)).doubleValue());
                }
                return result;
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<Object> a = (List<Object>) values.get(0).evaluate(context);
                        List<Object> b = (List<Object>) values.get(1).evaluate(context);
                        List<Object> result = new ArrayList<>();
                        for (int i = 0; i < a.size(); i++) {
                            result.add(((Number) a.get(i)).doubleValue() - ((Number) b.get(i)).doubleValue());
                        }
                        return result;
                    }
                }.getClass());
            }
        });

        // Vector multiplication (with a scalar)
        context.addFunction(new NativeFunction("vector.mul", List.of(Variable.Type.LIST, Variable.Type.DOUBLE), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<Object> a = (List<Object>) args.get(0);
                double b = (double) args.get(1);

                List<Object> result = new ArrayList<>();
                for (int i = 0; i < a.size(); i++) {
                    result.add(((Number) a.get(i)).doubleValue() * b);
                }

                return result;
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<Object> a = (List<Object>) values.get(0).evaluate(context);
                        double b = (double) values.get(1).evaluate(context);

                        List<Object> result = new ArrayList<>();
                        for (int i = 0; i < a.size(); i++) {
                            result.add(((Number) a.get(i)).doubleValue() * b);
                        }

                        return result;
                    }
                }.getClass());
            }
        });

        // Vector division (by a scalar)
        context.addFunction(new NativeFunction("vector.div", List.of(Variable.Type.LIST, Variable.Type.DOUBLE), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<Object> a = (List<Object>) args.get(0);
                double b = (double) args.get(1);

                List<Object> result = new ArrayList<>();
                for (int i = 0; i < a.size(); i++) {
                    result.add(((Number) a.get(i)).doubleValue() / b);
                }

                return result;
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<Object> a = (List<Object>) values.get(0).evaluate(context);
                        double b = (double) values.get(1).evaluate(context);

                        List<Object> result = new ArrayList<>();
                        for (int i = 0; i < a.size(); i++) {
                            result.add(((Number) a.get(i)).doubleValue() / b);
                        }

                        return result;
                    }
                }.getClass());
            }
        });

        // Vector dot product
        context.addFunction(new NativeFunction("vector.dot", List.of(Variable.Type.LIST, Variable.Type.LIST), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<Object> a = (List<Object>) args.get(0);
                List<Object> b = (List<Object>) args.get(1);

                double result = 0;
                for (int i = 0; i < a.size(); i++) {
                    result += ((Number) a.get(i)).doubleValue() * ((Number) b.get(i)).doubleValue();
                }

                return result;
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<Object> a = (List<Object>) values.get(0).evaluate(context);
                        List<Object> b = (List<Object>) values.get(1).evaluate(context);

                        double result = 0;
                        for (int i = 0; i < a.size(); i++) {
                            result += ((Number) a.get(i)).doubleValue() * ((Number) b.get(i)).doubleValue();
                        }

                        return result;
                    }
                }.getClass());
            }
        });

        // Vector cross product
        context.addFunction(new NativeFunction("vector.cross", List.of(Variable.Type.LIST, Variable.Type.LIST), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<Object> a = (List<Object>) args.get(0);
                List<Object> b = (List<Object>) args.get(1);

                List<Object> result = new ArrayList<>();
                result.add(((Number) a.get(1)).doubleValue() * ((Number) b.get(2)).doubleValue() - ((Number) a.get(2)).doubleValue() * ((Number) b.get(1)).doubleValue());
                result.add(((Number) a.get(2)).doubleValue() * ((Number) b.get(0)).doubleValue() - ((Number) a.get(0)).doubleValue() * ((Number) b.get(2)).doubleValue());
                result.add(((Number) a.get(0)).doubleValue() * ((Number) b.get(1)).doubleValue() - ((Number) a.get(1)).doubleValue() * ((Number) b.get(0)).doubleValue());

                return result;
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<Object> a = (List<Object>) values.get(0).evaluate(context);
                        List<Object> b = (List<Object>) values.get(1).evaluate(context);

                        List<Object> result = new ArrayList<>();
                        result.add(((Number) a.get(1)).doubleValue() * ((Number) b.get(2)).doubleValue() - ((Number) a.get(2)).doubleValue() * ((Number) b.get(1)).doubleValue());
                        result.add(((Number) a.get(2)).doubleValue() * ((Number) b.get(0)).doubleValue() - ((Number) a.get(0)).doubleValue() * ((Number) b.get(2)).doubleValue());
                        result.add(((Number) a.get(0)).doubleValue() * ((Number) b.get(1)).doubleValue() - ((Number) a.get(1)).doubleValue() * ((Number) b.get(0)).doubleValue());

                        return result;
                    }
                }.getClass());
            }
        });

        // Vector magnitude
        context.addFunction(new NativeFunction("vector.mag", List.of(Variable.Type.LIST), Variable.Type.DOUBLE) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<Object> a = (List<Object>) args.get(0);

                double result = 0;
                for (int i = 0; i < a.size(); i++) {
                    result += Math.pow(((Number) a.get(i)).doubleValue(), 2);
                }

                return Math.sqrt(result);
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<Object> a = (List<Object>) values.get(0).evaluate(context);

                        double result = 0;
                        for (int i = 0; i < a.size(); i++) {
                            result += Math.pow(((Number) a.get(i)).doubleValue(), 2);
                        }

                        return Math.sqrt(result);
                    }
                }.getClass());
            }
        });

        // Vector normalization
        context.addFunction(new NativeFunction("vector.normalize", List.of(Variable.Type.LIST), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<Object> a = (List<Object>) args.get(0);

                double mag = 0;
                for (int i = 0; i < a.size(); i++) {
                    mag += Math.pow(((Number) a.get(i)).doubleValue(), 2);
                }
                mag = Math.sqrt(mag);

                List<Object> result = new ArrayList<>();
                for (int i = 0; i < a.size(); i++) {
                    result.add(((Number) a.get(i)).doubleValue() / mag);
                }

                return result;
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<Object> a = (List<Object>) values.get(0).evaluate(context);

                        double mag = 0;
                        for (int i = 0; i < a.size(); i++) {
                            mag += Math.pow(((Number) a.get(i)).doubleValue(), 2);
                        }
                        mag = Math.sqrt(mag);

                        List<Object> result = new ArrayList<>();
                        for (int i = 0; i < a.size(); i++) {
                            result.add(((Number) a.get(i)).doubleValue() / mag);
                        }

                        return result;
                    }
                }.getClass());
            }
        });

        // Matrix addition
        context.addFunction(new NativeFunction("matrix.add", List.of(Variable.Type.LIST, Variable.Type.LIST), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<List<Object>> a = (List<List<Object>>) args.get(0);
                List<List<Object>> b = (List<List<Object>>) args.get(1);

                List<List<Object>> result = new ArrayList<>();
                for (int i = 0; i < a.size(); i++) {
                    List<Object> row = new ArrayList<>();
                    for (int j = 0; j < a.get(i).size(); j++) {
                        row.add(((Number) a.get(i).get(j)).doubleValue() + ((Number) b.get(i).get(j)).doubleValue());
                    }
                    result.add(row);
                }

                return result;
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<List<Object>> a = (List<List<Object>>) values.get(0).evaluate(context);
                        List<List<Object>> b = (List<List<Object>>) values.get(1).evaluate(context);

                        List<List<Object>> result = new ArrayList<>();
                        for (int i = 0; i < a.size(); i++) {
                            List<Object> row = new ArrayList<>();
                            for (int j = 0; j < a.get(i).size(); j++) {
                                row.add(((Number) a.get(i).get(j)).doubleValue() + ((Number) b.get(i).get(j)).doubleValue());
                            }
                            result.add(row);
                        }

                        return result;
                    }
                }.getClass());
            }
        });

        // Matrix subtraction
        context.addFunction(new NativeFunction("matrix.sub", List.of(Variable.Type.LIST, Variable.Type.LIST), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<List<Object>> a = (List<List<Object>>) args.get(0);
                List<List<Object>> b = (List<List<Object>>) args.get(1);

                List<List<Object>> result = new ArrayList<>();
                for (int i = 0; i < a.size(); i++) {
                    List<Object> row = new ArrayList<>();
                    for (int j = 0; j < a.get(i).size(); j++) {
                        row.add(((Number) a.get(i).get(j)).doubleValue() - ((Number) b.get(i).get(j)).doubleValue());
                    }
                    result.add(row);
                }

                return result;
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<List<Object>> a = (List<List<Object>>) values.get(0).evaluate(context);
                        List<List<Object>> b = (List<List<Object>>) values.get(1).evaluate(context);

                        List<List<Object>> result = new ArrayList<>();
                        for (int i = 0; i < a.size(); i++) {
                            List<Object> row = new ArrayList<>();
                            for (int j = 0; j < a.get(i).size(); j++) {
                                row.add(((Number) a.get(i).get(j)).doubleValue() - ((Number) b.get(i).get(j)).doubleValue());
                            }
                            result.add(row);
                        }

                        return result;
                    }
                }.getClass());
            }
        });

        // Matrix scalar multiplication
        context.addFunction(new NativeFunction("matrix.mulScalar", List.of(Variable.Type.LIST, Variable.Type.DOUBLE), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<List<Object>> a = (List<List<Object>>) args.get(0);
                double b = (double) args.get(1);

                List<List<Object>> result = new ArrayList<>();
                for (int i = 0; i < a.size(); i++) {
                    List<Object> row = new ArrayList<>();
                    for (int j = 0; j < a.get(i).size(); j++) {
                        row.add(((Number) a.get(i).get(j)).doubleValue() * b);
                    }
                    result.add(row);
                }

                return result;
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<List<Object>> a = (List<List<Object>>) values.get(0).evaluate(context);
                        double b = (double) values.get(1).evaluate(context);

                        List<List<Object>> result = new ArrayList<>();
                        for (int i = 0; i < a.size(); i++) {
                            List<Object> row = new ArrayList<>();
                            for (int j = 0; j < a.get(i).size(); j++) {
                                row.add(((Number) a.get(i).get(j)).doubleValue() * b);
                            }
                            result.add(row);
                        }

                        return result;
                    }
                }.getClass());
            }
        });

        // Matrix multiplication
        context.addFunction(new NativeFunction("matrix.mul", List.of(Variable.Type.LIST, Variable.Type.LIST), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<List<Object>> a = (List<List<Object>>) args.get(0);
                List<List<Object>> b = (List<List<Object>>) args.get(1);

                List<List<Object>> result = new ArrayList<>();
                for (int i = 0; i < a.size(); i++) {
                    List<Object> row = new ArrayList<>();
                    for (int j = 0; j < b.get(0).size(); j++) {
                        double sum = 0;
                        for (int k = 0; k < a.get(i).size(); k++) {
                            sum += ((Number) a.get(i).get(k)).doubleValue() * ((Number) b.get(k).get(j)).doubleValue();
                        }
                        row.add(sum);
                    }
                    result.add(row);
                }

                return result;
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<List<Object>> a = (List<List<Object>>) values.get(0).evaluate(context);
                        List<List<Object>> b = (List<List<Object>>) values.get(1).evaluate(context);

                        List<List<Object>> result = new ArrayList<>();
                        for (int i = 0; i < a.size(); i++) {
                            List<Object> row = new ArrayList<>();
                            for (int j = 0; j < b.get(0).size(); j++) {
                                double sum = 0;
                                for (int k = 0; k < a.get(i).size(); k++) {
                                    sum += ((Number) a.get(i).get(k)).doubleValue() * ((Number) b.get(k).get(j)).doubleValue();
                                }
                                row.add(sum);
                            }
                            result.add(row);
                        }

                        return result;
                    }
                }.getClass());
            }
        });

        // Matrix transpose
        context.addFunction(new NativeFunction("matrix.transpose", List.of(Variable.Type.LIST), Variable.Type.LIST) {
            @Override
            public Object call(ILocalContext context, List<Object> args) {
                List<List<Object>> a = (List<List<Object>>) args.get(0);

                List<List<Object>> result = new ArrayList<>();
                for (int i = 0; i < a.get(0).size(); i++) {
                    List<Object> row = new ArrayList<>();
                    for (int j = 0; j < a.size(); j++) {
                        row.add(a.get(j).get(i));
                    }
                    result.add(row);
                }

                return result;
            }

            @Override
            public <T extends BFunction> Class<T> getBakedFunction() {
                return (Class<T>) (new BFunction() {
                    @Override
                    public Object evaluate(ILocalContext context) {
                        List<List<Object>> a = (List<List<Object>>) values.get(0).evaluate(context);

                        List<List<Object>> result = new ArrayList<>();
                        for (int i = 0; i < a.get(0).size(); i++) {
                            List<Object> row = new ArrayList<>();
                            for (int j = 0; j < a.size(); j++) {
                                row.add(a.get(j).get(i));
                            }
                            result.add(row);
                        }

                        return result;
                    }
                }.getClass());
            }
        });
    }
}
