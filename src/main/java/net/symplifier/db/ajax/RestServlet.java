package net.symplifier.db.ajax;

import net.symplifier.db.*;
import net.symplifier.db.annotations.Rpc;
import net.symplifier.db.annotations.RpcParameter;
import net.symplifier.db.exceptions.DatabaseException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ranjan on 8/24/15.
 */
public class RestServlet extends HttpServlet {
  public static final SimpleDateFormat ISO8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") {{
    this.setTimeZone(TimeZone.getTimeZone("UTC"));
  }};

  private Model jsonToModel(ModelStructure model, JSONObject object) {
//    long id = object.optLong("id", 0);
//    ModelInstance obj;
//    if (id == 0) {
//      obj = model.create();
//    } else {
//      obj = model.get(id);
//    }
//
//
//    for(String key:object.keySet()) {
//      Object res = object.get(key);
//      if (res instanceof JSONObject) {
//        // We expect a Column.Reference or HasMany here
//        Relation relation = model.getRelation(key);
//        if (relation instanceof Column.Reference) {
//          obj.set((Column.Reference)relation,
//                  jsonToModel(((Column.Reference) relation).getTargetType()
//                          , (JSONObject)res));
//        } else if (relation instanceof Relation.HasMany) {
//          obj.add((Relation.HasMany)relation,
//                  jsonToModel(((Relation.HasMany)relation).getTargetType(),
//                          (JSONObject)res));
//
//        }
//      } else if (res instanceof JSONArray) {
//        // We expect a HasMany relationship here
//        model.getRelation(key);
//        Relation relation = model.getRelation(key);
//        if (relation instanceof Relation.HasMany) {
//          JSONArray ar = (JSONArray)res;
//          for(int i=0; i<ar.length(); ++i) {
//            JSONObject o = ((JSONArray) res).optJSONObject(i);
//            obj.add((Relation.HasMany)relation,
//                    jsonToModel(((Relation.HasMany)relation).getTargetType(), o));
//          }
//        }
//      } else {
//        // We expect a column here
//        Column column = model.getColumn(key);
//        obj.set(column, toModelValue(column, res));
//      }
//    }

    return null;
  }

  private ModelStructure getModel(HttpServletRequest request) {
    String uri = request.getPathInfo();
    // Strip out the starting and trailing '/'
    uri = StringUtils.strip(uri, "/");

    // First try to find out the model structure
    return Schema.get().getModelStructure(uri);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // let's see if this is an rpc call
    String uri = request.getPathInfo();
    // Strip out the starting and trailing '/'
    uri = StringUtils.strip(uri, "/");

    if (uri.startsWith("rpc/")) {
      doRPC(uri.substring(4), request, response);
    }
  }

  private void doRPC(String modelName, HttpServletRequest request, HttpServletResponse response) throws IOException {
    ModelStructure model = Schema.get().getModelStructure(modelName);
    if (model == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "The resource was not found on the system");
      return;
    }

    // First get the id of the model record
    long id;
    try {
      id = Long.parseLong(request.getParameter("id"));
    } catch(NumberFormatException e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid resource id " + request.getParameter("id"));
      return;
    }

    Class<? extends Model> modelClass = model.getType();
    String method = request.getParameter("method");
    JSONTokener tokener = new JSONTokener(request.getParameter("parameters"));
    Object tmp = tokener.nextValue();
    if (!(tmp instanceof JSONObject)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameters not found for RPC");
      return;
    }
    JSONObject reqParameters = (JSONObject)tmp;

    for(Method rMethod:modelClass.getMethods()) {
      Rpc rpc = rMethod.getAnnotation(Rpc.class);
      if (rpc == null || !rpc.value().equals(method)) {
        continue;
      }

      // We got ourselves a method to execute
      Object parameters[] = new Object[rMethod.getParameterCount()];
      int i=0;
      for(Parameter rParameter:rMethod.getParameters()) {
        RpcParameter ann = rParameter.getAnnotation(RpcParameter.class);
        if (ann == null) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "RPC method does not have proper RpcParameter annotation");
          return;
        }

        String parameterName = ann.value();
        if (!reqParameters.has(parameterName)) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter " + parameterName + " not provided with the request");
          return;
        }

        Class<?> parameterType = rParameter.getType();
        try {
          if (reqParameters.isNull(parameterName)) {
            parameters[i] = null;
          } else if (parameterType == String.class) {
            parameters[i] = reqParameters.getString(parameterName);
          } else if (parameterType == Integer.TYPE || parameterType == Integer.class) {
            parameters[i] = reqParameters.getInt(parameterName);
          } else if (parameterType == Date.class) {
            parameters[i] = ISO8601_FORMAT.parse(reqParameters.getString(parameterName));
          } else if (parameterType == Boolean.TYPE || parameterType == Boolean.class) {
            parameters[i] = reqParameters.getBoolean(parameterName);
          } else if (parameterType == Double.TYPE || parameterType == Double.class) {
            parameters[i] = reqParameters.getDouble(parameterName);
          } else if (parameterType == Long.TYPE || parameterType == Long.class) {
            parameters[i] = reqParameters.getLong(parameterName);
          } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter " + parameterName + " is not a supported type (String, Integer, Date, Boolean, Double, Long)");
            return;
          }
        } catch(JSONException | ParseException e) {
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter " + parameterName + " is not compatible with the request");
          return;
        }
      }


      String res = "";
      try {
        if (id == 0) {
          if ((rMethod.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
            // Call the static method
            res = rMethod.invoke(null, parameters).toString();
          } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The method is not declared static");
            return;
          }
        } else {
          if ((rMethod.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The method is declared static");
            return;
          } else {
            Model obj = Schema.get().find(modelClass, id);
            if (obj == null) {
              response.sendError(HttpServletResponse.SC_NOT_FOUND, "The resource with id " + id + " is not found");
              return;
            }
            res = rMethod.invoke(obj, parameters).toString();
          }
        }
      } catch(IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        return;
      }
      response.setHeader("ContentType", "application/json");
      response.getWriter().write(res);
    }
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ModelStructure model = getModel(request);

    if (model == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "The resource was not found on the system");
      return;
    }

    long id;
    try {
      id = Long.parseLong(request.getParameter("id"));
    } catch(NumberFormatException e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid resource id " + request.getParameter("id"));
      return;
    }

    Model record = model.get(id);

    // If a type and child Id is provided then we need to remove an intermediate table
    String type = request.getParameter("type");
    if (type != null) {
      Reference childType = record.getStructure().getRelation(type);
      if (childType instanceof Relation.HasMany && childType.getIntermediateTable() != null) {
        Relation.HasMany childRelation = (Relation.HasMany) childType;
        long childId = Long.parseLong(request.getParameter("child"));
        record.deleteChild(childRelation, childId);
      } else {
        throw new DatabaseException("Trying to delete a child record on " + record.getStructure() + " of " + childType.getRelationName() + " which does not have an intermediate table", null);
      }
    } else {
      record.delete();
    }

    JSONObject res = new JSONObject();
    res.put("id", id);
    response.getWriter().write(res.toString());
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
    ModelStructure model = getModel(request);

    if (model == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "The resource was not found on the System");
      return;
    }

    JSONTokener tokener = new JSONTokener(request.getReader());
    Object v = tokener.nextValue();
    if (v instanceof JSONObject) {
      JSONObject source = (JSONObject)v;
      Model record;
      try {
        record = createRecord(model, source);
        record.save();
      } catch(Exception e) {
        e.printStackTrace();
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().print(e.getMessage());
        response.flushBuffer();
        return;
      }

      response.setContentType("application/json");
      response.getWriter().write(record.toJSON().toString());
    } else if (v instanceof JSONArray) {
      JSONArray list = (JSONArray)v;
      JSONArray res = new JSONArray();
      for(int i=0; i<list.length(); ++i) {
        JSONObject source = list.getJSONObject(i);
        Model record = null;
        try {
          record = createRecord(model, source);
          record.save();
        } catch(Exception e) {
          e.printStackTrace();
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().print(e.getMessage());
          response.flushBuffer();
          return;
        }
        res.put(record.toJSON());
      }
      response.setContentType("application/json");
      response.getWriter().write(res.toString());
    } else {
      response.setContentType("text/plain");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid data");
      response.flushBuffer();
    }
  }

  private Model createRecord(ModelStructure model, JSONObject source) throws Exception {
    Model rec = null;
    // First let's see if there is an id field on the JSON, which means an
    // update otherwise an insert
    if (source.has(model.getPrimaryKeyField())) {
      long id = source.optLong(model.getPrimaryKeyField(), 0);
      if (id > 0) {
        rec = model.get(id);
      }
    }
    if (rec == null) {
      rec = model.create();
    }

    for(String key:source.keySet()) {
      JSONArray arr = source.optJSONArray(key);
      Object res = source.get(key);
      if (arr != null) {
        // We expect a relationship here
        Reference ref = model.getRelation(key);

        if (!(ref instanceof Relation.HasMany)) {
          throw new Exception("The data for relationship '"
                  + model.getTableName() + "." + key
                  + " is supposed to be a HasMany but was not");
        }
        // We got an array instance
        // need to be careful here, we might just get id's here
        // if the id is negative, then that means the record needs to be removed
        // which happens only in case of many-to-many relationship
        for(int i=0; i<((JSONArray) res).length(); ++i) {
          Object v = ((JSONArray)res).get(i);
          if (v instanceof JSONObject) {
            // Looks like we got our-selves an object to work
            rec.add((Relation.HasMany)ref,
                    createRecord(ref.getTargetType(), (JSONObject)v));
          } else {
            System.out.println(v);
          }
        }
      } else {
        JSONObject obj = source.optJSONObject(key);
        if (obj != null) {
          Reference ref = model.getRelation(key);
          // Make sure the reference is a Column.Reference
          if (!(ref instanceof Column.Reference)) {
            throw new Exception("The data for relationship '"
                    + model.getTableName() + "." + key
                    + " is supposed to be a Column.Reference but was not");
          }

          // In some cases the target type might be one of the child models, in
          // which case the type has to be provided with the data
          ModelStructure targetType = ref.getTargetType();
          if (obj.has("_type")) {
            targetType = targetType.getSchema().getModelStructure(obj.getString("_type"));
          }

          // recursively load the reference
          rec.setReference((Column.Reference) ref,
                  createRecord(targetType, (JSONObject) res));
        } else {
          // looks like a primitive field, we will get a string and parse the data
          // based on the column type
          Column col = model.getColumn(key);
          if (col != null) {

            // Handle null values
            if (source.isNull(key)) {
              rec.set(col, null);
            } else {
              String val = source.optString(key);
              if (val == null) {
                rec.set(col, null);
              } else {
                Class type = col.getValueType();
                if (type == String.class) {
                  rec.set(col, val);
                } else if (type == Integer.class) {
                  try {
                    rec.set(col, Integer.parseInt(val));
                  } catch (NumberFormatException e) {
                    throw new Exception("Expected an integer for '"
                            + model.getTableName() + "." + key
                            + " but found '" + val + "'");
                  }
                } else if (type == Long.class) {
                  try {
                    rec.set(col, Long.parseLong(val));
                  } catch (NumberFormatException e) {
                    throw new Exception("Expected a long for '"
                            + model.getTableName() + "." + key
                            + " but found '" + val + "'");
                  }
                } else if (type == Float.class) {
                  try {
                    rec.set(col, Float.parseFloat(val));
                  } catch (NumberFormatException e) {
                    throw new Exception("Expected a float for '"
                            + model.getTableName() + "." + key
                            + " but found '" + val + "'");
                  }
                } else if (type == Double.class) {
                  try {
                    rec.set(col, Double.parseDouble(val));
                  } catch (NumberFormatException e) {
                    throw new Exception("Expected a double for '"
                            + model.getTableName() + "." + key
                            + " but found '" + val + "'");
                  }
                } else if (type == Boolean.class) {
                  // No exception is thrown by parseBoolean
                  rec.set(col, Boolean.parseBoolean(val));
                } else if (type == Date.class) {
                  Date dateValue;
                  try {
                    dateValue = new Date(LocalDate.parse(val).atTime(0, 0)
                            .atZone(ZoneId.systemDefault())
                            .toEpochSecond() * 1000);
                  } catch (DateTimeParseException e1) {
                    try {
                      dateValue = new Date(LocalDateTime.parse(val)
                              .atZone(ZoneId.systemDefault())
                              .toEpochSecond() * 1000);
                    } catch (DateTimeParseException e2) {
                      try {
                        dateValue = new Date(ZonedDateTime
                                .parse(val, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                .toEpochSecond() * 1000);
                      } catch (DateTimeParseException e3) {
                        try {
                          dateValue = new Date(LocalTime.parse(val).toSecondOfDay() * 1000);
                        } catch (DateTimeParseException e4) {
                          throw new Exception("Expected a date/time for '"
                                  + model.getTableName() + "." + key
                                  + " but found '" + val + "'");
                        }
                      }
                    }
                  }
                  rec.set(col, dateValue);
                } else if (type == byte[].class) {
                  try {
                    rec.set(col, Base64.getDecoder().decode(val));
                  } catch (IllegalArgumentException e) {
                    rec.set(col, val.getBytes(StandardCharsets.UTF_8));
//                  throw new Exception("Expected a BLOB data encoded in BASE64 for '"
//                          + model.getTableName() + "." + key
//                          + " but found '" + val + "'");
                  }
                }
              }
            }
          }
        }
      }
    }

    return rec;
  }
}
