package com.sk89q.minecraft.util.args;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sk89q.minecraft.util.args.spi.Setter;

public class ClassParser {
    public void parse(Object obj, ArgsParser args) {
        for(Class<?> cls = obj.getClass(); cls != null; cls = cls.getSuperclass()) {
            for(Method method : cls.getDeclaredMethods()) {
                {
                    Option option = method.getAnnotation(Option.class);
                    if(option != null) {
                        Class<? extends Setter> setter = option.setter();
                        if(setter == Setter.class) {
                            args.addOption(new MethodSetter(cls, method), option);
                        } else {
                            Constructor<? extends Setter> constructor = null;
                            try {
                                constructor = setter.getConstructor(Class.class, Method.class);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            if(constructor != null) {
                                try {
                                    Setter s = constructor.newInstance(cls, method);
                                    args.addOption(s, option);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                {
                    Argument arg = method.getAnnotation(Argument.class);
                    if(arg != null) {
                        Class<? extends Setter> setter = arg.setter();
                        if(setter == Setter.class) {
                            args.addArgument(new MethodSetter(cls, method), arg);
                        } else {
                            Constructor<? extends Setter> constructor = null;
                            try {
                                constructor = setter.getConstructor(Class.class, Method.class);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            if(constructor != null) {
                                try {
                                    Setter s = constructor.newInstance(cls, method);
                                    args.addArgument(s, arg);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
            
            for(Field field : cls.getDeclaredFields()) {
                {
                    Option option = field.getAnnotation(Option.class);
                    if(option != null) {
                        Class<? extends Setter> setter = option.setter();
                        if(setter == Setter.class) {
                            args.addOption(new FieldSetter(cls, field), option);
                        } else {
                            Constructor<? extends Setter> constructor = null;
                            try {
                                constructor = setter.getConstructor(Class.class, Field.class);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            if(constructor != null) {
                                try {
                                    Setter s = constructor.newInstance(cls, field);
                                    args.addOption(s, option);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                {
                    Argument arg = field.getAnnotation(Argument.class);
                    if(arg != null) {
                        Class<? extends Setter> setter = arg.setter();
                        if(setter == Setter.class) {
                            args.addArgument(new FieldSetter(cls, field), arg);
                        } else {
                            Constructor<? extends Setter> constructor = null;
                            try {
                                constructor = setter.getConstructor(Class.class, Field.class);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            if(constructor != null) {
                                try {
                                    Setter s = constructor.newInstance(cls, field);
                                    args.addArgument(s, arg);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
