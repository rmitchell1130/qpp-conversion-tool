package gov.cms.qpp.conversion.model;

import gov.cms.qpp.conversion.util.ProgramContext;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class manages the available transformation handlers. Currently it takes
 * the XPATH that the handler will transform.
 * <p>
 * R is the stored and return interface type.
 * V is the key type to access the registered values.
 *
 * @author David Uselmann
 */
public class Registry<R> {
	private static final Logger DEV_LOG = LoggerFactory.getLogger(Registry.class);

	// For now this is static and can be refactored into an instance
	// variable when/if we have an orchestrator that instantiates an registry
	/**
	 * This will be an XPATH string to converter handler registration Since
	 * Converter was taken for the main stub, I chose Handler for now.
	 */
	private Map<ComponentKey, Class<? extends R>> registryMap;

	private Class<? extends Annotation> annotationClass;

	/**
	 * initialize and configure the registry
	 */
	public Registry(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
		load();
	}

	/**
	 * load or reload registry contents
	 */
	private void load() {
		init();
		registerAnnotatedHandlers();
	}

	/**
	 * This is a helper method used for testing. Singletons have trouble in
	 * testing if they cannot be reset. It is package access to only allow
	 * classes in the same package, like tests, have access.
	 */
	void init() {
		registryMap = new HashMap<>();
	}

	/**
	 * This method will scan all classes for the annotation for
	 * TransformHandlers that need registration.
	 */
	@SuppressWarnings("unchecked")
	private void registerAnnotatedHandlers() {
		Reflections reflections = new Reflections("gov.cms");
		Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotationClass);

		for (Class<?> annotatedClass : annotatedClasses) {
			for (ComponentKey key : getComponentKeys(annotatedClass)) {
				register(key, (Class<R>) annotatedClass);
			}
		}
	}

	Set<ComponentKey> getComponentKeys(Class<?> annotatedClass) {
		Annotation annotation = annotatedClass.getAnnotation(annotationClass);
		Set<ComponentKey> values = new HashSet<>();

		if (annotation instanceof Decoder) {
			Decoder decoder = (Decoder) annotation;
			values.add(new ComponentKey(decoder.value(), decoder.program()));
		}
		if (annotation instanceof Encoder) {
			Encoder encoder = (Encoder) annotation;
			values.add(new ComponentKey(encoder.value(), encoder.program()));
		}
		if (annotation instanceof Validator) {
			Validator validator = (Validator) annotation;
			values.add(new ComponentKey(validator.value(), validator.program()));
		}
		return values;
	}

	/**
	 * This method will return a proper top level handler for the given XPATH
	 * Later iteration will examine the XPATH startsWith and return a most
	 * appropriate handler
	 *
	 * @param registryKey String
	 */
	public R get(TemplateId registryKey) {
		return instantiateHandler(findHandler(registryKey));
	}

	private R instantiateHandler(Class<? extends R> handlerClass) {
		try {
			if (handlerClass == null) {
				return null;
			}
			return handlerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			DEV_LOG.warn("Unable to instantiate the class", e);
			return null;
		}
	}

	public Set<R> inclusiveGet(TemplateId registryKey) {
		return findHandlers(registryKey).stream()
				.map(this::instantiateHandler)
				.collect(Collectors.toSet());
	}

	/**
	 * Retrieve a handler for the given template id
	 *
	 * @param registryKey template id
	 * @return handler i.e. {@link Validator}, {@link Decoder} or {@link Encoder}
	 */
	private Class<? extends R> findHandler(TemplateId registryKey) {
		return findHandlers(registryKey).stream()
				.findFirst()
				.orElse(null);
	}

	private Set<Class<? extends R>> findHandlers(TemplateId registryKey) {
		Set<Class<? extends R>> handlers = new LinkedHashSet<>();
		ComponentKey program = new ComponentKey(registryKey, ProgramContext.get());
		ComponentKey general = new ComponentKey(registryKey, Program.ALL);
		Stream.of(program, general).forEach(key -> {
			Class<? extends R> handler = registryMap.get(key);
			if (handler != null) {
				handlers.add(handler);
			}
		});
		return handlers;
	}

	/**
	 * Means ot register a new transformation handler
	 *
	 * @param registryKey key that identifies a component i.e. a {@link Validator}, {@link Decoder} or {@link Encoder}
	 * @param handler the keyed {@link Validator}, {@link Decoder} or {@link Encoder}
	 */
	void register(ComponentKey registryKey, Class<? extends R> handler) {
		DEV_LOG.debug("Registering " + handler.getName() + " to '" + registryKey + "' for "
				+ annotationClass.getSimpleName() + ".");
		// This could be a class or class name and instantiated on lookup
		if (registryMap.containsKey(registryKey)) {
			DEV_LOG.error("Duplicate registered handler for " + registryKey
						+ " both " + registryMap.get(registryKey).getName()
						+ " and " + handler.getName());
		}
		
		registryMap.put(registryKey, handler);
	}

	public int size() {
		return registryMap.size();
	}
}