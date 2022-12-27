package io.github.lmikoto.railgun.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.github.lmikoto.railgun.entity.SimpleAnnotation;
import io.github.lmikoto.railgun.entity.SimpleClass;
import io.github.lmikoto.railgun.entity.SimpleField;
import io.github.lmikoto.railgun.entity.SimpleMethod;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liuyang
 * 2021/3/6 4:23 下午
 */
@Slf4j
public class Appender {


    public String process(SimpleClass model, String oldCode){
        CompilationUnit unit = Optional.ofNullable(oldCode).map(StaticJavaParser::parse).orElse(new CompilationUnit());
        TypeDeclaration<?> type = Optional.ofNullable(unit.getTypes()).flatMap(NodeList::getFirst).orElseGet(() ->{
            TypeDeclaration declaration = new ClassOrInterfaceDeclaration();
            unit.addType(declaration);
            return declaration;
        });

        buildPackage(unit, JavaUtils.getPackageName(model.getName()));

        buildClass(type,model);

        buildExtend(type,model.getExtend(),model.getImports());

        buildImplements(type,model.getImpl(),model.getImports());

        buildClassAnnotations(type,model.getAnnotations(),model.getImports());

        buildFiled(type,model.getFields(), model.getImports());

        buildMethod(type,model.getMethods(),model.getImports());

        buildImport(unit,model.getImports());
        return unit.toString();
    }

    private void buildFiled(TypeDeclaration<?> type, LinkedHashMap<String, SimpleField> fields, Set<String> imports) {
        if(!(type instanceof ClassOrInterfaceDeclaration)){
            return;
        }

        if(Objects.isNull(fields)){
            return;
        }

        ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration)type;
        Set<String> existed = declaration.getFields().stream()
                .map(FieldDeclaration::getVariables).map(i -> i.get(0)).map(VariableDeclarator::getNameAsString).collect(Collectors.toSet());

        Set<Map.Entry<String, SimpleField>> entries = fields.entrySet();
        for(Map.Entry<String, SimpleField> entry: entries){
            if(existed.contains(entry.getKey())){
                continue;
            }
            SimpleField fieldData = entry.getValue();
            List<SimpleAnnotation> simpleAnnotations = fieldData.getAnnotations();
            SimpleClass filedType = fieldData.getClazz();

            FieldDeclaration fieldDeclaration;
            if (StringUtils.isNotEmpty(fieldData.getExpr())) {
                fieldDeclaration = declaration.addField(filedType.getSimpleName(), entry.getKey() +
                        " = " + fieldData.getExpr());
            } else {
                fieldDeclaration = declaration.addField(filedType.getSimpleName(), entry.getKey());
            }
            NodeList<AnnotationExpr> annoNodes = new NodeList<>();
            if (CollectionUtils.isNotEmpty(simpleAnnotations)) {
                for(SimpleAnnotation a : simpleAnnotations){
                    if (StringUtils.isEmpty(a.getExpr())) {
                        log.error("注解表达式为空" + JsonUtils.toPrettyJson(a));
                        continue;
                    }
                    AnnotationExpr expr = StaticJavaParser.parseAnnotation(a.getExpr());
                    imports.add(a.getName());
                    annoNodes.add(expr);
                }
            }
            if (StringUtils.isNotEmpty(fieldData.getComment())) {
                fieldDeclaration.setBlockComment("*" + fieldData.getComment());
            }
            fieldDeclaration.setAnnotations(annoNodes);
            if (CollectionUtils.isNotEmpty(fieldData.getModifiers())) {
                Modifier.Keyword[] keywords = fieldData.getModifiers().toArray(new Modifier.Keyword[]{});
                fieldDeclaration.setModifiers(keywords);
            } else {
                fieldDeclaration.setModifiers(Modifier.Keyword.PUBLIC);
            }
        }
    }

    private void buildMethod(TypeDeclaration<?> type, List<SimpleMethod> methods, Set<String> imports) {
        if(!(type instanceof ClassOrInterfaceDeclaration)){
            return;
        }
        ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration)type;
        Set<String> existed = declaration.getMethods().stream().map(MethodDeclaration::getNameAsString).collect(Collectors.toSet());
        type.getFields().forEach(field -> {
            if (field.getModifiers().size() == 1 && field.getModifiers().contains(Modifier.privateModifier())) {
                if (existed.contains("set" + StringUtils.underlineToCamel(field
                        .getVariable(0).getNameAsString(), true))) {
                    return;
                }
                MethodDeclaration getter = field.createGetter();
                type.remove(getter);
                type.addMember(getter);
                if (existed.contains("get" + StringUtils.underlineToCamel(field
                        .getVariable(0).getNameAsString(), true))) {
                    return;
                }
                MethodDeclaration setter = field.createSetter();
                type.remove(setter);
                type.addMember(setter);
            }
        });
        if(CollectionUtils.isEmpty(methods)){
            return;
        }
        // todo 支持重载
        methods.forEach(m->{
            if(!existed.contains(m.getName())){
                MethodDeclaration methodDeclaration = new MethodDeclaration();
                methodDeclaration.setName(m.getName());
                String returnType = Optional.ofNullable(m.getType()).map(SimpleClass::getSimpleName).orElse("void");
                methodDeclaration.setType(returnType);
                if (CollectionUtils.isNotEmpty(m.getModifiers())) {
                    Modifier.Keyword[] modifiers = m.getModifiers().toArray(new Modifier.Keyword[]{});
                    methodDeclaration.setModifiers(modifiers);
                } else {
                    methodDeclaration.setModifiers(Modifier.Keyword.PUBLIC);
                }

                if(Objects.nonNull(m.getParams())){
                    List<SimpleField> params = m.getParams();
                    for (SimpleField param: params){
                        methodDeclaration.addParameter(param.getClazz().getSimpleName(), param.getName());
                    }
                }

                if(CollectionUtils.isNotEmpty(m.getAnnotations())){
                    for(SimpleAnnotation simpleAnnotation: m.getAnnotations()){
                        if (StringUtils.isEmpty(simpleAnnotation.getExpr())) {
                            log.error("注解表达式为空" + JsonUtils.toPrettyJson(simpleAnnotation));
                            continue;
                        }
                        AnnotationExpr annotationExpr = StaticJavaParser.parseAnnotation(simpleAnnotation.getExpr());
                        methodDeclaration.addAnnotation(annotationExpr);
                    }
                }

                if(CollectionUtils.isNotEmpty(m.getLine())){
                    BlockStmt body = new BlockStmt();
                    NodeList<Statement> statements = new NodeList<>();
                    for(String line: m.getLine()){
                        Statement statement = StaticJavaParser.parseStatement(line);
                        statements.add(statement);
                    }
                    body.setStatements(statements);
                    methodDeclaration.setBody(body);
                }

                type.addMember(methodDeclaration);
            }
        });

    }

    private void buildExtend(TypeDeclaration<?> type, List<SimpleClass> extend, Set<String> imports) {
        if(!(type instanceof ClassOrInterfaceDeclaration)){
            return;
        }
        if(CollectionUtils.isEmpty(extend)){
            return;
        }
        ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration)type;
        Set<String> existed = declaration.getExtendedTypes().stream().map(ClassOrInterfaceType::getNameAsString).collect(Collectors.toSet());
        extend.forEach(s->{
            String simpleName = JavaUtils.getSimpleName(s.getName());
            if(!existed.contains(simpleName)){
                declaration.addExtendedType(simpleName);
                imports.add(s.getName());
            }
        });
    }

    private void buildImplements(TypeDeclaration<?> type, List<SimpleClass> impls, Set<String> imports) {
        if(!(type instanceof ClassOrInterfaceDeclaration)){
            return;
        }
        if(CollectionUtils.isEmpty(impls)){
            return;
        }
        ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration)type;
        Set<String> existed = declaration.getImplementedTypes().stream().map(ClassOrInterfaceType::getNameAsString)
                .collect(Collectors.toSet());
        impls.forEach(s->{
            String simpleName = JavaUtils.getSimpleName(s.getName());
            if(!existed.contains(simpleName)){
                declaration.addImplementedType(simpleName);
                imports.add(s.getName());
            }
        });
    }

    private void buildClassAnnotations(TypeDeclaration<?> type, List<SimpleAnnotation> annotations, Set<String> imports) {
        if(CollectionUtils.isEmpty(annotations)){
            return;
        }
        NodeList<AnnotationExpr> ans = Optional.ofNullable(type.getAnnotations()).orElse(new NodeList<>());
        Set<String> existed = ans.stream().map(AnnotationExpr::getNameAsString).collect(Collectors.toSet());
        annotations.forEach(a->{
            if(!existed.contains(a.getSimpleName())){
                    if (StringUtils.isEmpty(a.getExpr())) {
                        log.error("注解表达式为空" + JsonUtils.toPrettyJson(a));
                        return;
                    }
                    AnnotationExpr expr = StaticJavaParser.parseAnnotation(a.getExpr());
                    type.addAnnotation(expr);
                    imports.add(a.getName());
            }
        });
    }

    private void buildImport(CompilationUnit unit, Collection<String> imports) {
        if(CollectionUtils.isEmpty(imports)){
            return;
        }
        NodeList<ImportDeclaration> importDeclarations = Optional.ofNullable(unit.getImports()).orElse(new NodeList<>());
        Set<String> existed = importDeclarations.stream().map(ImportDeclaration::getNameAsString).collect(Collectors.toSet());

        imports.stream().distinct().forEach(i->{
            if(!existed.contains(i)){
                ImportDeclaration importDeclaration = new ImportDeclaration(i,false,false);
                importDeclarations.add(importDeclaration);
            }
        });
        unit.setImports(importDeclarations);
    }

    private void buildPackage(CompilationUnit unit, String packageName) {
        if(Objects.isNull(unit.getPackageDeclaration()) || !unit.getPackageDeclaration().isPresent()){
            PackageDeclaration packageDeclaration = new PackageDeclaration();
            packageDeclaration.setName(packageName);
            unit.setPackageDeclaration(packageDeclaration);
        }
    }

    private void buildClass(TypeDeclaration<?> type, SimpleClass model) {
        if(Objects.isNull(type.getName()) || "empty".equals(type.getNameAsString())){
            type.setName(JavaUtils.getSimpleName(model.getName()));
            if (StringUtils.isNotEmpty(model.getComment())) {
                type.setBlockComment("*" + model.getComment());
            }
            type.setModifiers(Modifier.Keyword.PUBLIC);
        }
    }

    public String process(String config,String oldCode){
        SimpleClass model = JsonUtils.fromJson(config,new JsonUtils.TypeReference<SimpleClass>() {
        });
        return process(model,oldCode);
    }

    @SneakyThrows
    public void fullProcess(String config,String path){
        File file = new File(path);
        String oldCode = null;
        if(file.exists()){
            oldCode = new String(Files.readAllBytes(Paths.get(path)));
        }
        String code = process(config,oldCode);
        Files.write(Paths.get(path),code.getBytes());
    }
}
