import { useState, useEffect, useRef, useCallback } from 'react'

// ─── Data ────────────────────────────────────────────────────────────────────

const NAV_LINKS = ['Inicio', 'Tienda', 'Impacto', 'Nosotros']

const PRODUCTS = [
  {
    id: 1,
    name: 'Casaca Älpafill Cusco',
    collection: 'Älpafill',
    price: 'S/ 259.90',
    impact: 'ABRIGO' as const,
    sizes: ['M', 'L', 'XL'],
    inStock: true,
    image: 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=480&h=600&fit=crop&auto=format',
  },
  {
    id: 2,
    name: 'Chaleco Älpafill',
    collection: 'Älpafill',
    price: 'S/ 219.90',
    impact: 'ABRIGO' as const,
    sizes: ['S'],
    inStock: true,
    image: 'https://images.unsplash.com/photo-1544022613-e87ca75a784a?w=480&h=600&fit=crop&auto=format',
  },
  {
    id: 3,
    name: 'Poncho Älpafill',
    collection: 'Älpafill',
    price: 'Ver catálogo',
    impact: 'ABRIGO' as const,
    sizes: ['Única'],
    inStock: true,
    image: 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=480&h=600&fit=crop&auto=format',
  },
  {
    id: 4,
    name: 'Gorro de lana Layo',
    collection: 'Comunidad',
    price: 'S/ 49.90',
    impact: 'ARBOL' as const,
    sizes: ['Única'],
    inStock: true,
    image: 'https://images.unsplash.com/photo-1576871337622-98d48d1cf531?w=480&h=600&fit=crop&auto=format',
  },
  {
    id: 5,
    name: 'Bufanda Paucartambo',
    collection: 'Comunidad',
    price: 'S/ 39.90',
    impact: 'ARBOL' as const,
    sizes: ['Única'],
    inStock: false,
    image: 'https://images.unsplash.com/photo-1607083206968-13611e3d76db?w=480&h=600&fit=crop&auto=format',
  },
  {
    id: 6,
    name: 'Mochila artesanal Höség',
    collection: 'Urbana',
    price: 'Ver catálogo',
    impact: 'ARBOL' as const,
    sizes: ['Única'],
    inStock: true,
    image: 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=480&h=600&fit=crop&auto=format',
  },
]

const IMPACT_COUNTERS = [
  { label: 'Abrigos entregados', value: 1248, icon: <CoatIcon /> },
  { label: 'Árboles plantados', value: 874, icon: <TreeIcon /> },
  { label: 'Comunidades alcanzadas', value: 5, icon: <CommunityIcon /> },
  { label: 'Pedidos con impacto', value: 2104, icon: <HeartIcon /> },
]

const COMMUNITIES = ['Omacha', 'Ccatca', 'Marcapata', 'Paucartambo', 'Layo']

// ─── Icons ───────────────────────────────────────────────────────────────────

function HomeIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 9.5L12 3l9 6.5V20a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9.5z" />
      <path d="M9 21V12h6v9" />
    </svg>
  )
}

function ShopIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" />
      <line x1="3" y1="6" x2="21" y2="6" />
      <path d="M16 10a4 4 0 01-8 0" />
    </svg>
  )
}

function ImpactNavIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
    </svg>
  )
}

function CartIcon({ count }: { count: number }) {
  return (
    <div className="relative">
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="9" cy="21" r="1" /><circle cx="20" cy="21" r="1" />
        <path d="M1 1h4l2.68 13.39a2 2 0 001.99 1.61h9.72a2 2 0 001.98-1.68l1.65-9.32H6" />
      </svg>
      {count > 0 && (
        <span className="absolute -top-1.5 -right-1.5 bg-[#C4622D] text-white text-[9px] font-semibold w-4 h-4 rounded-full flex items-center justify-center leading-none">
          {count}
        </span>
      )}
    </div>
  )
}

function AccountIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>
  )
}

function CoatIcon() {
  return (
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round">
      <path d="M4 4l4 2 4-4 4 4 4-2v16H4V4z" />
      <path d="M12 2v8" />
    </svg>
  )
}

function TreeIcon() {
  return (
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 22V12" />
      <path d="M5 12h14L12 2 5 12z" />
      <path d="M3 17h18L12 7 3 17z" />
    </svg>
  )
}

function CommunityIcon() {
  return (
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M23 21v-2a4 4 0 00-3-3.87" />
      <path d="M16 3.13a4 4 0 010 7.75" />
    </svg>
  )
}

function HeartIcon() {
  return (
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" />
    </svg>
  )
}

function ChevronLeft() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="15 18 9 12 15 6" />
    </svg>
  )
}

function ChevronRight() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="9 18 15 12 9 6" />
    </svg>
  )
}

function ArrowRight() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <line x1="5" y1="12" x2="19" y2="12" /><polyline points="12 5 19 12 12 19" />
    </svg>
  )
}

// ─── Utility ─────────────────────────────────────────────────────────────────

function useIntersection(ref: React.RefObject<Element | null>, options?: IntersectionObserverInit) {
  const [intersecting, setIntersecting] = useState(false)
  useEffect(() => {
    const el = ref.current
    if (!el) return
    const obs = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) { setIntersecting(true); obs.disconnect() }
    }, options)
    obs.observe(el)
    return () => obs.disconnect()
  }, [ref, options])
  return intersecting
}

function useCountUp(target: number, active: boolean, duration = 1600) {
  const [value, setValue] = useState(0)
  useEffect(() => {
    if (!active) return
    let start = 0
    const step = target / (duration / 16)
    const timer = setInterval(() => {
      start = Math.min(start + step, target)
      setValue(Math.floor(start))
      if (start >= target) clearInterval(timer)
    }, 16)
    return () => clearInterval(timer)
  }, [active, target, duration])
  return value
}

// ─── Badge ────────────────────────────────────────────────────────────────────

function ImpactBadge({ type }: { type: 'ABRIGO' | 'ARBOL' }) {
  if (type === 'ABRIGO') {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold"
        style={{ background: 'rgba(254,240,232,0.72)', color: '#C4622D', border: '1px solid rgba(245,205,179,0.6)', backdropFilter: 'blur(10px) saturate(160%)', WebkitBackdropFilter: 'blur(10px) saturate(160%)', boxShadow: 'inset 0 1px 0 rgba(255,255,255,0.4)' }}>
        <svg width="11" height="11" viewBox="0 0 24 24" fill="currentColor"><path d="M4 4l4 2 4-4 4 4 4-2v16H4V4z" /></svg>
        1 abrigo
      </span>
    )
  }
  return (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold"
      style={{ background: 'rgba(232,240,232,0.72)', color: '#5A7A5C', border: '1px solid rgba(181,208,184,0.6)', backdropFilter: 'blur(10px) saturate(160%)', WebkitBackdropFilter: 'blur(10px) saturate(160%)', boxShadow: 'inset 0 1px 0 rgba(255,255,255,0.4)' }}>
      <svg width="11" height="11" viewBox="0 0 24 24" fill="currentColor"><path d="M12 22V12M5 12h14L12 2 5 12zM3 17h18L12 7 3 17z" /></svg>
      1 árbol
    </span>
  )
}

// ─── ProductCard ─────────────────────────────────────────────────────────────

function ProductCard({ product }: { product: typeof PRODUCTS[0] }) {
  const [hovered, setHovered] = useState(false)
  const dimmed = !product.inStock

  return (
    <div
      className="flex-shrink-0 w-full rounded-2xl overflow-hidden flex flex-col"
      style={{
        background: 'var(--card)',
        border: '1px solid var(--border)',
        opacity: dimmed ? 0.65 : 1,
        transition: 'box-shadow 0.2s, transform 0.2s',
        boxShadow: hovered && !dimmed ? '0 8px 32px rgba(44,24,16,0.12)' : '0 1px 4px rgba(44,24,16,0.06)',
        transform: hovered && !dimmed ? 'translateY(-3px)' : 'none',
      }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      <div className="relative overflow-hidden" style={{ aspectRatio: '4/5', background: '#EDE6D6' }}>
        <img
          src={product.image}
          alt={product.name}
          className="w-full h-full object-cover"
          style={{ transition: 'transform 0.4s', transform: hovered && !dimmed ? 'scale(1.04)' : 'scale(1)' }}
        />
        <div className="absolute top-3 left-3">
          <ImpactBadge type={product.impact} />
        </div>
        {dimmed && (
          <div className="absolute inset-0 flex items-center justify-center"
            style={{ background: 'rgba(245,240,232,0.55)' }}>
            <span className="px-3 py-1 rounded-full text-xs font-semibold"
              style={{ background: 'rgba(44,24,16,0.75)', color: '#F5F0E8' }}>
              Agotado
            </span>
          </div>
        )}
      </div>

      <div className="p-4 flex flex-col gap-2 flex-1">
        <div>
          <p className="text-[11px] font-medium tracking-wider uppercase" style={{ color: 'var(--muted-foreground)' }}>
            {product.collection}
          </p>
          <h3 className="text-[15px] font-semibold leading-tight mt-0.5" style={{ fontFamily: 'var(--font-display)', color: 'var(--foreground)' }}>
            {product.name}
          </h3>
        </div>

        <div className="flex items-center justify-between mt-auto pt-1">
          <span className="text-[15px] font-semibold" style={{ color: 'var(--foreground)' }}>{product.price}</span>
          <div className="flex gap-1">
            {product.sizes.map(s => (
              <span key={s} className="text-[10px] font-medium px-1.5 py-0.5 rounded"
                style={{ background: 'var(--secondary)', color: 'var(--muted-foreground)', border: '1px solid var(--border)' }}>
                {s}
              </span>
            ))}
          </div>
        </div>

        {product.inStock ? (
          <div className="flex gap-2 mt-1">
            <button className="flex-1 py-2 rounded-xl text-sm font-semibold transition-all"
              style={{ background: 'var(--secondary)', color: 'var(--foreground)', border: '1px solid var(--border)' }}
              onMouseEnter={e => { e.currentTarget.style.background = 'var(--muted)' }}
              onMouseLeave={e => { e.currentTarget.style.background = 'var(--secondary)' }}>
              Ver detalle
            </button>
            <button
              className="flex-1 py-2 rounded-xl text-sm font-semibold transition-all flex items-center justify-center gap-1.5"
              style={{ background: '#C4622D', color: '#FDFAF5' }}
              onMouseEnter={e => { e.currentTarget.style.background = '#A8501E' }}
              onMouseLeave={e => { e.currentTarget.style.background = '#C4622D' }}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="9" cy="21" r="1" /><circle cx="20" cy="21" r="1" />
                <path d="M1 1h4l2.68 13.39a2 2 0 001.99 1.61h9.72a2 2 0 001.98-1.68l1.65-9.32H6" />
              </svg>
              Agregar
            </button>
          </div>
        ) : (
          <div className="w-full mt-1 py-2 rounded-xl text-sm font-semibold text-center"
            style={{ background: 'var(--muted)', color: 'var(--muted-foreground)' }}>
            Agotado
          </div>
        )}
      </div>
    </div>
  )
}

// ─── FeaturedCarousel ─────────────────────────────────────────────────────────

function FeaturedCarousel() {
  const [index, setIndex] = useState(0)
  const [paused, setPaused] = useState(false)
  const touchStartX = useRef<number | null>(null)

  const VISIBLE_DESKTOP = 4
  const VISIBLE_TABLET = 2
  const total = PRODUCTS.length

  const prev = useCallback(() => setIndex(i => (i - 1 + total) % total), [total])
  const next = useCallback(() => setIndex(i => (i + 1) % total), [total])

  useEffect(() => {
    if (paused) return
    const t = setInterval(next, 5000)
    return () => clearInterval(t)
  }, [paused, next])

  const getVisibleCount = () => {
    if (typeof window === 'undefined') return VISIBLE_DESKTOP
    if (window.innerWidth < 768) return 1.4
    if (window.innerWidth < 1200) return VISIBLE_TABLET
    return VISIBLE_DESKTOP
  }

  const [visibleCount, setVisibleCount] = useState(4)
  useEffect(() => {
    const update = () => setVisibleCount(getVisibleCount())
    update()
    window.addEventListener('resize', update)
    return () => window.removeEventListener('resize', update)
  }, [])

  const translateX = -(index * (100 / visibleCount))

  return (
    <section id="tienda" className="py-16 md:py-24" style={{ background: 'var(--background)' }}>
      <div className="max-w-7xl mx-auto px-4 md:px-8">
        <div className="mb-8 flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4">
          <div>
            <p className="text-xs font-semibold tracking-[0.2em] uppercase mb-2" style={{ color: 'var(--muted-foreground)' }}>Productos destacados</p>
            <h2 className="text-3xl md:text-4xl font-semibold leading-tight" style={{ fontFamily: 'var(--font-display)', color: 'var(--foreground)' }}>
              Cada prenda<br className="hidden sm:block" /> abriga dos veces.
            </h2>
          </div>
          <div className="flex gap-2">
            <button onClick={prev}
              className="w-10 h-10 rounded-full flex items-center justify-center transition-all"
              style={{ border: '1px solid var(--border)', background: 'var(--card)', color: 'var(--foreground)' }}
              onMouseEnter={e => e.currentTarget.style.background = 'var(--secondary)'}
              onMouseLeave={e => e.currentTarget.style.background = 'var(--card)'}>
              <ChevronLeft />
            </button>
            <button onClick={next}
              className="w-10 h-10 rounded-full flex items-center justify-center transition-all"
              style={{ border: '1px solid var(--border)', background: 'var(--card)', color: 'var(--foreground)' }}
              onMouseEnter={e => e.currentTarget.style.background = 'var(--secondary)'}
              onMouseLeave={e => e.currentTarget.style.background = 'var(--card)'}>
              <ChevronRight />
            </button>
          </div>
        </div>

        <div
          className="overflow-hidden"
          onMouseEnter={() => setPaused(true)}
          onMouseLeave={() => setPaused(false)}
          onTouchStart={e => { touchStartX.current = e.touches[0].clientX }}
          onTouchEnd={e => {
            if (touchStartX.current === null) return
            const dx = e.changedTouches[0].clientX - touchStartX.current
            if (dx < -40) next()
            else if (dx > 40) prev()
            touchStartX.current = null
          }}
        >
          <div
            className="flex gap-4"
            style={{ transform: `translateX(calc(${translateX}% - ${index * 16 / visibleCount}px))`, transition: 'transform 0.5s cubic-bezier(0.4,0,0.2,1)' }}
          >
            {PRODUCTS.map(p => (
              <div key={p.id} style={{ flex: `0 0 calc(${100 / visibleCount}% - ${16 * (visibleCount - 1) / visibleCount}px)` }}>
                <ProductCard product={p} />
              </div>
            ))}
          </div>
        </div>

        <div className="flex justify-center gap-1.5 mt-6">
          {PRODUCTS.map((_, i) => (
            <button key={i} onClick={() => setIndex(i)}
              className="rounded-full transition-all"
              style={{
                width: i === index ? '20px' : '7px',
                height: '7px',
                background: i === index ? 'var(--primary)' : 'var(--muted)',
              }} />
          ))}
        </div>
      </div>
    </section>
  )
}

// ─── ImpactSteps ─────────────────────────────────────────────────────────────

function ImpactSteps() {
  return (
    <section id="impacto" className="py-16 md:py-24" style={{ background: 'var(--secondary)' }}>
      <div className="max-w-5xl mx-auto px-4 md:px-8">
        <div className="text-center mb-12">
          <p className="text-xs font-semibold tracking-[0.2em] uppercase mb-2" style={{ color: 'var(--muted-foreground)' }}>Así funciona</p>
          <h2 className="text-3xl md:text-4xl font-semibold" style={{ fontFamily: 'var(--font-display)', color: 'var(--foreground)' }}>
            Tu compra tiene nombre y apellido.
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 md:gap-8">
          {[
            {
              step: '01',
              title: 'Compras',
              desc: 'Eliges una prenda o accesorio HÖSÉG y completas tu pedido en nuestra tienda.',
              icon: (
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#C4622D" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" /><line x1="3" y1="6" x2="21" y2="6" />
                  <path d="M16 10a4 4 0 01-8 0" />
                </svg>
              ),
            },
            {
              step: '02',
              title: 'Registramos',
              desc: 'Con el número de tu boleta vinculamos tu abrigo o árbol a una comunidad real de Cusco.',
              icon: (
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#C4622D" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" /><polyline points="14 2 14 8 20 8" />
                  <line x1="16" y1="13" x2="8" y2="13" /><line x1="16" y1="17" x2="8" y2="17" /><polyline points="10 9 9 9 8 9" />
                </svg>
              ),
            },
            {
              step: '03',
              title: 'Entregamos',
              desc: 'Nuestra ONG aliada coordina la entrega. Tú puedes ver el resultado en línea con tu boleta.',
              icon: (
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#C4622D" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
                </svg>
              ),
            },
          ].map((item, i) => (
            <div key={i} className="relative flex flex-col gap-4 p-6 rounded-2xl"
              style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
              <span className="text-[11px] font-bold tracking-[0.25em] uppercase" style={{ color: 'var(--muted-foreground)' }}>{item.step}</span>
              <div className="w-14 h-14 rounded-2xl flex items-center justify-center"
                style={{ background: '#FEF0E8' }}>
                {item.icon}
              </div>
              <h3 className="text-xl font-semibold" style={{ fontFamily: 'var(--font-display)', color: 'var(--foreground)' }}>
                {item.title}
              </h3>
              <p className="text-sm leading-relaxed" style={{ color: 'var(--muted-foreground)' }}>{item.desc}</p>
              {i < 2 && (
                <div className="hidden md:block absolute top-1/2 -right-4 z-10" style={{ transform: 'translateY(-50%)' }}>
                  <ArrowRight />
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

// ─── CounterCard ─────────────────────────────────────────────────────────────

function CounterCard({ label, value, icon }: { label: string; value: number; icon: React.ReactNode }) {
  const ref = useRef<HTMLDivElement>(null)
  const visible = useIntersection(ref, { threshold: 0.3 })
  const count = useCountUp(value, visible)

  return (
    <div ref={ref} className="flex flex-col items-center gap-3 p-6 rounded-2xl text-center"
      style={{ background: 'var(--card)', border: '1px solid var(--border)' }}>
      <div className="w-12 h-12 rounded-xl flex items-center justify-center" style={{ background: '#FEF0E8', color: '#C4622D' }}>
        {icon}
      </div>
      <div className="text-4xl font-bold tabular-nums" style={{ fontFamily: 'var(--font-display)', color: 'var(--foreground)' }}>
        {count.toLocaleString('es-PE')}
      </div>
      <p className="text-sm font-medium" style={{ color: 'var(--muted-foreground)' }}>{label}</p>
    </div>
  )
}

// ─── ImpactCounters ───────────────────────────────────────────────────────────

function ImpactCounters() {
  return (
    <section className="py-16 md:py-24" style={{ background: 'var(--background)' }}>
      <div className="max-w-5xl mx-auto px-4 md:px-8">
        <div className="text-center mb-12">
          <p className="text-xs font-semibold tracking-[0.2em] uppercase mb-2" style={{ color: 'var(--muted-foreground)' }}>Impacto real</p>
          <h2 className="text-3xl md:text-4xl font-semibold" style={{ fontFamily: 'var(--font-display)', color: 'var(--foreground)' }}>
            Cada número es una persona,<br className="hidden sm:block" /> cada árbol tiene raíces.
          </h2>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-10">
          {IMPACT_COUNTERS.map(c => (
            <CounterCard key={c.label} label={c.label} value={c.value} icon={c.icon} />
          ))}
        </div>

        <div className="flex flex-wrap justify-center gap-2">
          <p className="w-full text-center text-xs font-semibold tracking-[0.15em] uppercase mb-1" style={{ color: 'var(--muted-foreground)' }}>
            Comunidades beneficiadas
          </p>
          {COMMUNITIES.map(c => (
            <span key={c} className="px-4 py-1.5 rounded-full text-sm font-medium"
              style={{ background: 'var(--secondary)', color: 'var(--foreground)', border: '1px solid var(--border)' }}>
              {c}
            </span>
          ))}
        </div>
      </div>
    </section>
  )
}

// ─── ImpactCTA ────────────────────────────────────────────────────────────────

function ImpactCTA() {
  const [boleta, setBoleta] = useState('')

  return (
    <section className="py-16 md:py-20" style={{ background: 'var(--secondary)' }}>
      <div className="max-w-2xl mx-auto px-4 md:px-8 text-center">
        <p className="text-xs font-semibold tracking-[0.2em] uppercase mb-3" style={{ color: 'var(--muted-foreground)' }}>Consulta tu impacto</p>
        <h2 className="text-3xl md:text-4xl font-semibold mb-4" style={{ fontFamily: 'var(--font-display)', color: 'var(--foreground)' }}>
          ¿Ya compraste? Mira a quién abrigaste.
        </h2>
        <p className="text-sm leading-relaxed mb-8 max-w-md mx-auto" style={{ color: 'var(--muted-foreground)' }}>
          Ingresa el número de tu boleta (ej. <strong>WEB-000110</strong>) y te mostramos a qué comunidad llegó tu donación. También puedes escanear el QR impreso en tu boleta.
        </p>
        <form className="flex flex-col sm:flex-row gap-3 max-w-sm mx-auto" onSubmit={e => { e.preventDefault(); /* redirect */ }}>
          <input
            type="text"
            placeholder="WEB-000110"
            value={boleta}
            onChange={e => setBoleta(e.target.value)}
            className="flex-1 px-4 py-3 rounded-xl text-sm outline-none transition-all"
            style={{
              background: 'var(--card)',
              border: '1px solid var(--border)',
              color: 'var(--foreground)',
              fontFamily: 'var(--font-body)',
            }}
            onFocus={e => e.target.style.borderColor = '#C4622D'}
            onBlur={e => e.target.style.borderColor = 'var(--border)'}
          />
          <button type="submit"
            className="px-6 py-3 rounded-xl text-sm font-semibold transition-all whitespace-nowrap"
            style={{ background: '#C4622D', color: '#FDFAF5' }}
            onMouseEnter={e => e.currentTarget.style.background = '#A8501E'}
            onMouseLeave={e => e.currentTarget.style.background = '#C4622D'}>
            Consultar
          </button>
        </form>
      </div>
    </section>
  )
}

// ─── AboutStrip ───────────────────────────────────────────────────────────────

function AboutStrip() {
  return (
    <section id="nosotros" className="py-16 md:py-24" style={{ background: 'var(--background)' }}>
      <div className="max-w-7xl mx-auto px-4 md:px-8">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-10 items-center">
          <div className="rounded-2xl overflow-hidden aspect-[4/3]" style={{ background: '#EDE6D6' }}>
            <img
              src="https://images.unsplash.com/photo-1761810833690-5b866a047c2c?w=800&h=600&fit=crop&auto=format"
              alt="Comunidad altoandina de Cusco"
              className="w-full h-full object-cover"
            />
          </div>
          <div className="flex flex-col gap-6">
            <p className="text-xs font-semibold tracking-[0.2em] uppercase" style={{ color: 'var(--muted-foreground)' }}>Quiénes somos</p>
            <h2 className="text-3xl md:text-4xl font-semibold leading-tight" style={{ fontFamily: 'var(--font-display)', color: 'var(--foreground)' }}>
              Ropa que calienta en los dos sentidos.
            </h2>
            <div className="flex flex-col gap-4 text-sm leading-relaxed" style={{ color: 'var(--muted-foreground)' }}>
              <p>
                HÖSÉG nació en Perú con una pregunta simple: ¿puede vender ropa de abrigo y al mismo tiempo asegurarse de que quienes más la necesitan también la tengan? La respuesta es sí, y lo probamos con cada pedido.
              </p>
              <p>
                Trabajamos con una ONG aliada que coordina la entrega de abrigos a comunidades altoandinas de Cusco y la plantación de cedros andinos. Cada donación es rastreable con el número de tu boleta.
              </p>
              <p>
                Somos una empresa de triple impacto: hacemos bien a las personas, al planeta y al negocio. Estamos en proceso de certificación B Corp.
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              {['Triple impacto', 'B Corp (en proceso)', 'ONG aliada', 'Trazabilidad 100%'].map(tag => (
                <span key={tag} className="px-3 py-1 rounded-full text-xs font-semibold"
                  style={{ background: 'var(--secondary)', color: 'var(--foreground)', border: '1px solid var(--border)' }}>
                  {tag}
                </span>
              ))}
            </div>
            <a href="#" className="inline-flex items-center gap-2 text-sm font-semibold transition-colors"
              style={{ color: '#C4622D' }}
              onMouseEnter={e => e.currentTarget.style.color = '#A8501E'}
              onMouseLeave={e => e.currentTarget.style.color = '#C4622D'}>
              Conoce más <ArrowRight />
            </a>
          </div>
        </div>
      </div>
    </section>
  )
}

// ─── Footer ───────────────────────────────────────────────────────────────────

function Footer() {
  return (
    <footer className="pb-28 md:pb-0" style={{ background: '#2C1810', color: '#D9CEB8' }}>
      <div className="max-w-7xl mx-auto px-4 md:px-8 py-14 md:py-16">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-10 mb-10">
          <div className="md:col-span-2 flex flex-col gap-4">
            <span className="text-2xl font-bold tracking-wide" style={{ fontFamily: 'var(--font-display)', color: '#F5F0E8' }}>HÖSÉG</span>
            <p className="text-sm leading-relaxed max-w-xs" style={{ color: '#A08878' }}>
              Buy One, Give One · Buy One, Plant One.<br />
              Ropa de abrigo peruana con triple impacto.
            </p>
            <div className="flex gap-3 mt-1">
              {['Instagram', 'Facebook', 'TikTok'].map(net => (
                <a key={net} href="#"
                  className="text-xs font-medium px-3 py-1 rounded-full transition-all"
                  style={{ border: '1px solid #4A3830', color: '#A08878' }}
                  onMouseEnter={e => { e.currentTarget.style.background = '#4A2E20'; e.currentTarget.style.color = '#F5F0E8' }}
                  onMouseLeave={e => { e.currentTarget.style.background = 'transparent'; e.currentTarget.style.color = '#A08878' }}>
                  {net}
                </a>
              ))}
            </div>
          </div>
          <div className="flex flex-col gap-3">
            <p className="text-xs font-semibold tracking-[0.15em] uppercase mb-1" style={{ color: '#7A6152' }}>Navegar</p>
            {['Tienda', 'Impacto', 'Nosotros', 'Consulta tu impacto'].map(l => (
              <a key={l} href="#" className="text-sm transition-colors" style={{ color: '#A08878' }}
                onMouseEnter={e => e.currentTarget.style.color = '#F5F0E8'}
                onMouseLeave={e => e.currentTarget.style.color = '#A08878'}>
                {l}
              </a>
            ))}
          </div>
          <div className="flex flex-col gap-3">
            <p className="text-xs font-semibold tracking-[0.15em] uppercase mb-1" style={{ color: '#7A6152' }}>Legal y contacto</p>
            {['Política de responsabilidad social', 'Términos y condiciones', 'Contacto por WhatsApp', 'hola@hoseg.pe'].map(l => (
              <a key={l} href="#" className="text-sm transition-colors" style={{ color: '#A08878' }}
                onMouseEnter={e => e.currentTarget.style.color = '#F5F0E8'}
                onMouseLeave={e => e.currentTarget.style.color = '#A08878'}>
                {l}
              </a>
            ))}
          </div>
        </div>
        <div className="pt-6" style={{ borderTop: '1px solid #3D2820' }}>
          <p className="text-xs" style={{ color: '#5A4038' }}>© 2026 HÖSÉG Store · 14-DIEZ S.A.C. · Hecho en Perú con propósito.</p>
        </div>
      </div>
    </footer>
  )
}

// ─── Navbar ───────────────────────────────────────────────────────────────────

const PILL_NAV_ITEMS = [
  { id: 'Inicio',  icon: <HomeIcon />,      label: 'Inicio'  },
  { id: 'Tienda',  icon: <ShopIcon />,      label: 'Tienda'  },
  { id: 'Impacto', icon: <ImpactNavIcon />, label: 'Impacto' },
  { id: 'cart',    icon: null,              label: 'Carrito' },
  { id: 'cuenta',  icon: <AccountIcon />,   label: 'Cuenta'  },
]

function Navbar({ cartCount }: { cartCount: number }) {
  const [scrolled, setScrolled] = useState(false)
  const [activeNav, setActiveNav] = useState('Inicio')

  // Sliding pill state: left offset + width inside the inner flex container
  const [pill, setPill] = useState({ left: 0, width: 56 })
  const prevIdxRef = useRef(0)
  const btnRefs = useRef<(HTMLButtonElement | null)[]>([])
  const innerRef = useRef<HTMLDivElement>(null)

  // Measure and set pill position for a given index
  const snapTo = useCallback((idx: number) => {
    const btn = btnRefs.current[idx]
    const inner = innerRef.current
    if (!btn || !inner) return
    setPill({ left: btn.offsetLeft, width: btn.offsetWidth })
  }, [])

  // On mount, initialise pill without animation
  useEffect(() => { snapTo(0) }, [snapTo])

  const handlePillClick = (id: string, idx: number) => {
    if (id === activeNav) return
    setActiveNav(id)

    const prevBtn = btnRefs.current[prevIdxRef.current]
    const nextBtn = btnRefs.current[idx]
    const inner = innerRef.current
    if (!prevBtn || !nextBtn || !inner) { prevIdxRef.current = idx; return }

    const goRight = idx > prevIdxRef.current
    const prevLeft   = prevBtn.offsetLeft
    const prevRight  = prevLeft + prevBtn.offsetWidth
    const nextLeft   = nextBtn.offsetLeft
    const nextRight  = nextLeft + nextBtn.offsetWidth

    // Phase 1: stretch to cover both buttons
    if (goRight) {
      setPill({ left: prevLeft,  width: nextRight - prevLeft  })
    } else {
      setPill({ left: nextLeft,  width: prevRight - nextLeft  })
    }

    // Phase 2: snap to target
    const t = setTimeout(() => {
      setPill({ left: nextLeft, width: nextBtn.offsetWidth })
    }, 220)

    prevIdxRef.current = idx
    return () => clearTimeout(t)
  }

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 60)
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  return (
    <>
      {/* Desktop Navbar */}
      <nav className="fixed top-0 left-0 right-0 z-50 hidden md:flex items-center justify-between px-8 h-16 transition-all duration-300"
        style={{
          background: scrolled ? 'rgba(245,240,232,0.72)' : 'transparent',
          backdropFilter: scrolled ? 'blur(20px) saturate(180%)' : 'none',
          WebkitBackdropFilter: scrolled ? 'blur(20px) saturate(180%)' : 'none',
          borderBottom: scrolled ? '1px solid rgba(217,206,184,0.5)' : '1px solid transparent',
          boxShadow: scrolled ? '0 2px 24px rgba(44,24,16,0.08)' : 'none',
        }}>
        <span className="text-xl font-bold tracking-wide" style={{ fontFamily: 'var(--font-display)', color: scrolled ? 'var(--foreground)' : '#F5F0E8' }}>
          HÖSÉG
        </span>

        <div className="flex items-center gap-7">
          {NAV_LINKS.map(link => (
            <a key={link} href={`#${link.toLowerCase()}`}
              onClick={() => setActiveNav(link)}
              className="text-sm font-medium transition-colors relative"
              style={{ color: scrolled ? (activeNav === link ? 'var(--primary)' : 'var(--foreground)') : (activeNav === link ? '#F5F0E8' : 'rgba(245,240,232,0.75)') }}>
              {link}
              {activeNav === link && (
                <span className="absolute -bottom-1 left-0 right-0 h-px rounded-full" style={{ background: '#C4622D' }} />
              )}
            </a>
          ))}
        </div>

        <div className="flex items-center gap-4">
          <a href="#" className="flex items-center gap-1.5 text-sm font-medium transition-colors"
            style={{ color: scrolled ? 'var(--foreground)' : 'rgba(245,240,232,0.85)' }}>
            <AccountIcon />
            <span className="hidden lg:inline">Ingresar</span>
          </a>
          <a href="#" style={{ color: scrolled ? 'var(--foreground)' : 'rgba(245,240,232,0.85)' }}>
            <CartIcon count={cartCount} />
          </a>
        </div>
      </nav>

      {/* Mobile top bar */}
      <nav className="fixed top-0 left-0 right-0 z-50 flex md:hidden items-center justify-between px-5 h-14 transition-all duration-300"
        style={{
          background: scrolled ? 'rgba(245,240,232,0.65)' : 'transparent',
          backdropFilter: scrolled ? 'blur(20px) saturate(180%)' : 'none',
          WebkitBackdropFilter: scrolled ? 'blur(20px) saturate(180%)' : 'none',
          borderBottom: scrolled ? '1px solid rgba(217,206,184,0.45)' : '1px solid transparent',
          boxShadow: scrolled ? '0 2px 16px rgba(44,24,16,0.07)' : 'none',
        }}>
        <span className="text-lg font-bold tracking-wide" style={{ fontFamily: 'var(--font-display)', color: scrolled ? 'var(--foreground)' : '#F5F0E8' }}>
          HÖSÉG
        </span>
        <a href="#" style={{ color: scrolled ? 'var(--foreground)' : 'rgba(245,240,232,0.85)' }}>
          <CartIcon count={cartCount} />
        </a>
      </nav>

      {/* Mobile pill nav */}
      <div className="fixed bottom-5 left-1/2 z-50 md:hidden"
        style={{ transform: 'translateX(-50%)' }}>
        <div style={{
          background: 'rgba(30,14,6,0.55)',
          backdropFilter: 'blur(24px) saturate(180%)',
          WebkitBackdropFilter: 'blur(24px) saturate(180%)',
          borderRadius: '999px',
          padding: '8px',
          border: '1px solid rgba(245,240,232,0.14)',
          boxShadow: '0 12px 40px rgba(44,24,16,0.4), inset 0 1px 0 rgba(245,240,232,0.12)',
        }}>
          {/* Inner container for measurement */}
          <div ref={innerRef} style={{ position: 'relative', display: 'flex', alignItems: 'center', gap: '2px' }}>
            {/* Sliding pill indicator */}
            <div style={{
              position: 'absolute',
              top: 0,
              bottom: 0,
              left: pill.left,
              width: pill.width,
              borderRadius: '999px',
              background: '#C4622D',
              transition: 'left 0.22s cubic-bezier(0.4,0,0.2,1), width 0.22s cubic-bezier(0.4,0,0.2,1)',
              pointerEvents: 'none',
              zIndex: 0,
            }} />

            {PILL_NAV_ITEMS.map((item, idx) => (
              <button
                key={item.id}
                ref={el => { btnRefs.current[idx] = el }}
                onClick={() => handlePillClick(item.id, idx)}
                style={{
                  position: 'relative',
                  zIndex: 1,
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '2px',
                  padding: '6px 12px',
                  borderRadius: '999px',
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  minWidth: '52px',
                  color: activeNav === item.id ? '#FDFAF5' : 'rgba(217,206,184,0.6)',
                  transition: 'color 0.18s',
                }}>
                {item.id === 'cart' ? <CartIcon count={cartCount} /> : item.icon}
                <span style={{ fontSize: '11px', fontWeight: 500, lineHeight: 1, fontFamily: 'var(--font-body)' }}>
                  {item.label}
                </span>
              </button>
            ))}
          </div>
        </div>
      </div>
    </>
  )
}

// ─── HeroBanner ───────────────────────────────────────────────────────────────

function HeroBanner() {
  return (
    <section id="inicio" className="relative w-full overflow-hidden" style={{ minHeight: '100svh' }}>
      <div className="absolute inset-0">
        <img
          src="https://images.unsplash.com/photo-1785810116787-489fb894b9f7?w=1440&h=900&fit=crop&auto=format"
          alt="Paisaje altoandino de Cusco"
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0" style={{ background: 'linear-gradient(to bottom, rgba(28,12,6,0.55) 0%, rgba(28,12,6,0.25) 50%, rgba(28,12,6,0.7) 100%)' }} />
      </div>

      <div className="relative z-10 flex flex-col justify-end min-h-[100svh] px-5 pb-16 md:px-16 md:pb-20 pt-20 max-w-7xl mx-auto">
        <div className="max-w-xl">
          <div className="flex flex-wrap gap-2 mb-6">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-semibold"
              style={{ background: 'rgba(196,98,45,0.45)', color: '#F5F0E8', backdropFilter: 'blur(12px) saturate(160%)', WebkitBackdropFilter: 'blur(12px) saturate(160%)', border: '1px solid rgba(245,240,232,0.2)' }}>
              Buy One, Give One
            </span>
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-semibold"
              style={{ background: 'rgba(90,122,92,0.45)', color: '#F5F0E8', backdropFilter: 'blur(12px) saturate(160%)', WebkitBackdropFilter: 'blur(12px) saturate(160%)', border: '1px solid rgba(245,240,232,0.2)' }}>
              Buy One, Plant One
            </span>
          </div>

          <h1 className="text-5xl md:text-6xl lg:text-7xl font-semibold leading-tight mb-5" style={{ fontFamily: 'var(--font-display)', color: '#F5F0E8' }}>
            Abriga a alguien más con cada compra.
          </h1>

          <p className="text-base md:text-lg leading-relaxed mb-8 max-w-sm" style={{ color: 'rgba(245,240,232,0.82)' }}>
            Empresa peruana de ropa de abrigo con triple impacto. Cada prenda llega a dos personas: a ti y a un niño de una comunidad altoandina de Cusco.
          </p>

          <div className="flex flex-col sm:flex-row gap-3">
            <a href="#tienda"
              className="inline-flex items-center justify-center gap-2 px-7 py-3.5 rounded-xl text-sm font-semibold transition-all"
              style={{ background: '#C4622D', color: '#FDFAF5' }}
              onMouseEnter={e => e.currentTarget.style.background = '#A8501E'}
              onMouseLeave={e => e.currentTarget.style.background = '#C4622D'}>
              Ver tienda
            </a>
            <a href="#impacto-cta"
              className="inline-flex items-center justify-center gap-2 px-7 py-3.5 rounded-xl text-sm font-semibold transition-all"
              style={{ background: 'rgba(245,240,232,0.12)', color: '#F5F0E8', backdropFilter: 'blur(16px) saturate(160%)', WebkitBackdropFilter: 'blur(16px) saturate(160%)', border: '1px solid rgba(245,240,232,0.28)', boxShadow: 'inset 0 1px 0 rgba(255,255,255,0.1)' }}
              onMouseEnter={e => e.currentTarget.style.background = 'rgba(245,240,232,0.22)'}
              onMouseLeave={e => e.currentTarget.style.background = 'rgba(245,240,232,0.12)'}>
              Consulta tu impacto
            </a>
          </div>
        </div>
      </div>
    </section>
  )
}

// ─── App ──────────────────────────────────────────────────────────────────────

export default function App() {
  const [cartCount] = useState(2)

  return (
    <div style={{ fontFamily: 'var(--font-body)', background: 'var(--background)', color: 'var(--foreground)' }}>
      <Navbar cartCount={cartCount} />
      <HeroBanner />
      <FeaturedCarousel />
      <ImpactSteps />
      <ImpactCounters />
      <div id="impacto-cta">
        <ImpactCTA />
      </div>
      <AboutStrip />
      <Footer />
    </div>
  )
}
