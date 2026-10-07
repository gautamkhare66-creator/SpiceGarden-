import { useEffect, useState } from 'react';
import {
  ArrowDown,
  ArrowLeft,
  ArrowRight,
  Check,
  ChefHat,
  Clock3,
  Instagram,
  Leaf,
  LogIn,
  MapPin,
  Minus,
  Plus,
  ShoppingBag,
  Sparkles,
  UserRound,
  X,
} from 'lucide-react';

const money = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const imageFallbacks = {
  default: 'https://images.unsplash.com/photo-1563379926898-05f4575a45d8?auto=format&fit=crop&w=800&q=80',
  Starter: 'https://images.unsplash.com/photo-1628294896516-344152572ee8?auto=format&fit=crop&w=800&q=80',
  Drinks: 'https://images.unsplash.com/photo-1546173159-315724a31696?auto=format&fit=crop&w=800&q=80',
};

function FoodImage({ src, category, alt, className, loading = 'lazy', fetchPriority }) {
  const fallback = imageFallbacks[category] || imageFallbacks.default;
  const [imageSource, setImageSource] = useState(src || fallback);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    setImageSource(src || fallback);
    setFailed(false);
  }, [src, fallback]);

  if (failed) {
    return <div className={`photo-placeholder ${className || ''}`} role="img" aria-label={alt}><ChefHat size={26} /></div>;
  }

  return <img className={className} src={imageSource} alt={alt} loading={loading} fetchPriority={fetchPriority} onError={() => {
    if (imageSource !== fallback) setImageSource(fallback);
    else setFailed(true);
  }} />;
}

async function request(path, options) {
  const method = options?.method?.toUpperCase() || 'GET';
  const headers = { 'Content-Type': 'application/json', ...options?.headers };
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const tokenResponse = await fetch('/api/csrf', { credentials: 'same-origin' });
    const { token } = await tokenResponse.json();
    headers['X-XSRF-TOKEN'] = token;
  }
  const response = await fetch(path, {
    ...options,
    credentials: 'same-origin',
    headers,
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(payload.message || 'Something went wrong. Please try again.');
  return payload;
}

function App() {
  const [menu, setMenu] = useState([]);
  const [category, setCategory] = useState('All');
  const [cart, setCart] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('spice-garden-cart') || '[]');
    } catch {
      return [];
    }
  });
  const [cartOpen, setCartOpen] = useState(false);
  const [reservationOpen, setReservationOpen] = useState(false);
  const [authOpen, setAuthOpen] = useState(false);
  const [authMode, setAuthMode] = useState('login');
  const [authError, setAuthError] = useState('');
  const [account, setAccount] = useState(null);
  const [notice, setNotice] = useState(null);
  const [loading, setLoading] = useState(true);
  const [menuError, setMenuError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    request('/api/menu')
      .then((items) => {
        setMenu(items);
        const availableIds = new Set(items.map((item) => item.id));
        setCart((current) => current.filter((item) => availableIds.has(item.id)));
      })
      .catch((error) => setMenuError(error.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    request('/api/auth/me')
      .then((result) => setAccount(result.authenticated ? result : null))
      .catch(() => setAccount(null));
  }, []);

  useEffect(() => {
    localStorage.setItem('spice-garden-cart', JSON.stringify(cart));
  }, [cart]);

  useEffect(() => {
    if (!cartOpen && !reservationOpen && !authOpen && !notice) return undefined;
    function closeOnEscape(event) {
      if (event.key === 'Escape') {
        setCartOpen(false);
        setReservationOpen(false);
        setAuthOpen(false);
        setNotice(null);
      }
    }
    window.addEventListener('keydown', closeOnEscape);
    return () => window.removeEventListener('keydown', closeOnEscape);
  }, [cartOpen, reservationOpen, authOpen, notice]);

  const categories = ['All', ...new Set(menu.map((item) => item.category))];
  const visibleMenu = category === 'All' ? menu : menu.filter((item) => item.category === category);
  const count = cart.reduce((sum, item) => sum + item.quantity, 0);
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

  function addToCart(dish) {
    setCart((current) => {
      const existing = current.find((item) => item.id === dish.id);
      return existing
        ? current.map((item) => item.id === dish.id ? { ...item, quantity: item.quantity + 1 } : item)
        : [...current, { ...dish, quantity: 1 }];
    });
  }

  function changeQuantity(id, amount) {
    setCart((current) => current
      .map((item) => item.id === id ? { ...item, quantity: item.quantity + amount } : item)
      .filter((item) => item.quantity > 0));
  }

  async function placeOrder(event) {
    event.preventDefault();
    setSubmitting(true);
    const form = new FormData(event.currentTarget);
    try {
      const order = await request('/api/orders', {
        method: 'POST',
        body: JSON.stringify({
          customerName: form.get('customerName'),
          email: form.get('email'),
          phone: form.get('phone'),
          orderType: form.get('orderType'),
          notes: form.get('notes'),
          items: cart.map((item) => ({ itemId: item.id, quantity: item.quantity })),
        }),
      });
      setCart([]);
      setCartOpen(false);
      setNotice({ title: 'Order received', detail: `Order #${order.id} is confirmed. We’ll be in touch shortly.` });
    } catch (error) {
      setNotice({ title: 'Could not place order', detail: error.message });
    } finally {
      setSubmitting(false);
    }
  }

  async function makeReservation(event) {
    event.preventDefault();
    setSubmitting(true);
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    try {
      const reservation = await request('/api/reservations', {
        method: 'POST',
        body: JSON.stringify({
          customerName: form.get('customerName'),
          email: form.get('email'),
          phone: form.get('phone'),
          dateTime: form.get('dateTime'),
          partySize: Number(form.get('partySize')),
          notes: form.get('notes'),
        }),
      });
      setReservationOpen(false);
      setNotice({ title: 'Your table is saved', detail: `Reservation #${reservation.id} is confirmed for ${reservation.partySize}.` });
      formElement.reset();
    } catch (error) {
      setNotice({ title: 'Could not reserve a table', detail: error.message });
    } finally {
      setSubmitting(false);
    }
  }

  async function authenticateAccount(event) {
    event.preventDefault();
    setSubmitting(true);
    setAuthError('');
    const form = new FormData(event.currentTarget);
    const registering = authMode === 'register';
    try {
      const result = await request(`/api/auth/${registering ? 'register' : 'login'}`, {
        method: 'POST',
        body: JSON.stringify({
          ...(registering ? { fullName: form.get('fullName') } : {}),
          email: form.get('email'),
          password: form.get('password'),
        }),
      });
      setAccount(result);
      setAuthOpen(false);
      setNotice({ title: registering ? 'Welcome to the table' : 'Welcome back', detail: `Signed in as ${result.email}.` });
    } catch (error) {
      setAuthError(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function signOut() {
    try {
      await request('/api/auth/logout', { method: 'POST' });
      setAccount(null);
      setNotice({ title: 'Signed out', detail: 'You can still order and reserve as a guest.' });
    } catch (error) {
      setNotice({ title: 'Could not sign out', detail: error.message });
    }
  }

  function scrollToMenu() {
    document.getElementById('menu')?.scrollIntoView({ behavior: 'smooth' });
  }

  return (
    <>
      <div className="announcement"><span><Sparkles size={13} /> GOOD FOOD, GOOD COMPANY</span><span>Open today · 11:30 am – 10:30 pm</span></div>
      <header className="site-header">
        <a className="wordmark" href="#top" aria-label="Spice Garden home"><span className="wordmark-mark"><Leaf size={20} /></span><span>spice garden<small>INDIAN KITCHEN & BAR</small></span></a>
        <nav className="main-nav" aria-label="Main navigation">
          <a href="#menu">The menu</a><a href="#story">Our story</a>
          <button className="nav-reserve" onClick={() => setReservationOpen(true)}>Book a table <ArrowRight size={15} /></button>
        </nav>
        {account ? <button className="account-trigger" onClick={signOut} aria-label={`Sign out ${account.fullName}`}><UserRound size={17} /><span>{account.fullName.split(' ')[0]}</span><small>Sign out</small></button> : <button className="account-trigger" onClick={() => { setAuthMode('login'); setAuthError(''); setAuthOpen(true); }}><LogIn size={17} /><span>Sign in</span></button>}
        <button className="cart-trigger" onClick={() => setCartOpen(true)} aria-label={`Open bag, ${count} items`}>
          <ShoppingBag size={18} /><span>Bag</span><b>{count}</b>
        </button>
      </header>

      <main id="top">
        <section className="hero">
          <div className="hero-copy">
            <div className="hero-kicker"><span className="kicker-line" /> FROM OUR KITCHEN, WITH HEART</div>
            <h1>A little more<br />spice. A lot more<br /><em>together.</em></h1>
            <p>Slow-cooked favourites, bright Indian flavours, and a seat at the table with your name on it.</p>
            <div className="hero-actions"><button className="primary-button" onClick={scrollToMenu}>Explore the menu <ArrowRight size={17} /></button><button className="quiet-button" onClick={() => setReservationOpen(true)}>Reserve your table</button></div>
            <div className="hero-footnote"><div className="avatar-stack"><span>SG</span><span>♥</span><span>+</span></div><span><strong>Made fresh, every day.</strong><br />Loved around the neighbourhood.</span></div>
          </div>
          <div className="hero-visual">
            <FoodImage src="https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=1500&q=88" alt="A vibrant Indian thali served fresh at the table" loading="eager" fetchPriority="high" />
            <div className="image-note"><span className="note-stamp"><ChefHat size={20} /></span><span>FRESH FROM<br />OUR KITCHEN</span></div>
            <div className="hero-caption"><span>01 / 03</span><span>THE SPICE GARDEN TABLE</span><span className="caption-rule" /></div>
          </div>
          <a className="scroll-cue" href="#menu"><ArrowDown size={15} /> SCROLL TO TASTE</a>
        </section>

        <section className="promise-strip" aria-label="Restaurant highlights">
          <div><Leaf size={18} /><span>Thoughtful ingredients</span></div><div><Clock3 size={18} /><span>Made to order</span></div><div><MapPin size={18} /><span>A table for everyone</span></div><div className="strip-rating"><span>★★★★★</span><small>NEIGHBOURHOOD FAVOURITE</small></div>
        </section>

        <section className="menu-section" id="menu">
          <div className="section-topline"><span>THE GOOD STUFF</span><span>01 — OUR MENU</span></div>
          <div className="menu-heading"><div><p className="eyebrow">Cooked with care</p><h2>Find your<br /><em>new favourite.</em></h2></div><p>Big, generous flavours. Ingredients we believe in. And absolutely no reason to share that last bite.</p></div>
          <div className="menu-controls"><div className="category-tabs" role="tablist" aria-label="Filter menu by category">
            {categories.map((item) => <button key={item} role="tab" aria-selected={category === item} className={category === item ? 'active' : ''} onClick={() => setCategory(item)}>{item}</button>)}
          </div><span className="menu-count">{loading ? 'Finding the good stuff…' : `${visibleMenu.length} DISHES, MADE FRESH`}</span></div>
          {menuError && <div className="status-message error-message">{menuError} <button onClick={() => window.location.reload()}>Try again</button></div>}
          {loading ? <div className="menu-loading">Setting the table…</div> : <div className="dish-grid">
            {visibleMenu.map((dish, index) => (
              <article className="dish-card" key={dish.id}>
                <div className="dish-image"><FoodImage src={dish.imageUrl} category={dish.category} alt={dish.name} loading="lazy" /><span className="dish-index">0{index + 1}</span><button className="add-dish" onClick={() => addToCart(dish)} aria-label={`Add ${dish.name} to bag`}><Plus size={19} /></button></div>
                <div className="dish-meta"><span>{dish.category}</span><span>{money.format(dish.price)}</span></div>
                <h3>{dish.name}</h3><p>{dish.description}</p>
              </article>
            ))}
          </div>}
          {!loading && !menuError && menu.length === 0 && <div className="menu-loading">Today’s menu is being prepared. Check back soon.</div>}
          <div className="menu-bottom"><span>THE BEST THINGS COME TO THE TABLE.</span><button className="text-button" onClick={() => setReservationOpen(true)}>Come dine with us <ArrowRight size={16} /></button></div>
        </section>

        <section className="story-section" id="story">
          <div className="story-photo"><FoodImage src="https://images.unsplash.com/photo-1559339352-11d035aa65de?auto=format&fit=crop&w=1200&q=85" alt="Warmly lit restaurant dining room ready for guests" /><span className="story-photo-caption">A PLACE AT OUR TABLE, ALWAYS.</span></div>
          <div className="story-copy"><p className="eyebrow">A little about us</p><h2>Good food is<br />how we say<br /><em>“come in.”</em></h2><p>Spice Garden began with a simple idea: the best meals are the ones that bring people closer. Our kitchen follows the seasons, honours the recipes we grew up with, and always makes room for one more.</p><div className="story-signoff"><span className="signature-mark">SG</span><span>With warmth,<br /><strong>The Spice Garden family</strong></span></div><button className="text-button" onClick={() => setReservationOpen(true)}>Save a seat <ArrowRight size={16} /></button></div>
        </section>

        <section className="visit-section"><div><p className="eyebrow">Your table is waiting</p><h2>Make tonight<br /><em>a little warmer.</em></h2><button className="primary-button" onClick={() => setReservationOpen(true)}>Book a table <ArrowRight size={17} /></button></div><div className="visit-details"><div><MapPin size={18} /><span><strong>Come find us</strong><br />12 Garden Lane, Your City</span></div><div><Clock3 size={18} /><span><strong>Come when you’re hungry</strong><br />Every day · 11:30 am – 10:30 pm</span></div><div><Instagram size={18} /><span><strong>Stay for a little longer</strong><br />@spicegarden.kitchen</span></div></div><span className="visit-decoration">SG</span></section>
      </main>

      <footer className="site-footer"><a className="wordmark footer-wordmark" href="#top"><span className="wordmark-mark"><Leaf size={19} /></span><span>spice garden<small>INDIAN KITCHEN & BAR</small></span></a><span>MADE WITH CARE. SERVED WITH LOVE.</span><span>© 2026 SPICE GARDEN</span></footer>

      {cartOpen && <div className="overlay" onMouseDown={(event) => event.target === event.currentTarget && setCartOpen(false)}>
        <aside className="side-panel" role="dialog" aria-modal="true" aria-labelledby="cart-title">
          <div className="panel-heading"><div><span className="eyebrow">A good choice</span><h2 id="cart-title">Your bag <span>({count})</span></h2></div><button className="icon-button" onClick={() => setCartOpen(false)} aria-label="Close bag"><X /></button></div>
          {cart.length === 0 ? <div className="empty-cart"><span className="empty-bag-icon"><ShoppingBag size={26} /></span><h3>Nothing in the bag. Yet.</h3><p>Something delicious is just a tap away.</p><button className="primary-button" onClick={() => { setCartOpen(false); scrollToMenu(); }}>Browse the menu <ArrowRight size={17} /></button></div> : <>
            <div className="cart-lines">{cart.map((item) => <div className="cart-line" key={item.id}><FoodImage src={item.imageUrl} category={item.category} alt="" /><div className="cart-line-info"><strong>{item.name}</strong><span>{money.format(item.price)}</span><div className="quantity-control"><button onClick={() => changeQuantity(item.id, -1)} aria-label={`Remove one ${item.name}`}><Minus size={13} /></button><span>{item.quantity}</span><button onClick={() => changeQuantity(item.id, 1)} aria-label={`Add one ${item.name}`}><Plus size={13} /></button></div></div><strong className="line-total">{money.format(item.price * item.quantity)}</strong></div>)}</div>
            <div className="cart-total"><span>Subtotal</span><strong>{money.format(total)}</strong></div><p className="cart-note">Made fresh after you order. Your kitchen is already getting excited.</p>
            <form className="checkout-form" onSubmit={placeOrder}><h3>Where should we send the good stuff?</h3><label>Your name<input name="customerName" autoComplete="name" required /></label><label>Email address<input name="email" type="email" autoComplete="email" required /></label><label>Phone number<input name="phone" type="tel" autoComplete="tel" required /></label><label>Order style<select name="orderType"><option value="pickup">I’ll pick it up</option><option value="delivery">Bring it to me</option></select></label><label>Anything we should know? <span>OPTIONAL</span><textarea name="notes" rows="2" /></label><button className="primary-button checkout-button" disabled={submitting}>{submitting ? 'Sending your order…' : <>Place order · {money.format(total)} <ArrowRight size={17} /></>}</button></form>
          </>}
        </aside>
      </div>}

      {reservationOpen && <div className="overlay" onMouseDown={(event) => event.target === event.currentTarget && setReservationOpen(false)}>
        <section className="reservation-panel" role="dialog" aria-modal="true" aria-labelledby="reservation-title"><div className="panel-heading"><div><span className="eyebrow">We saved you a seat</span><h2 id="reservation-title">Let’s make a night of it.</h2></div><button className="icon-button" onClick={() => setReservationOpen(false)} aria-label="Close reservation"><X /></button></div><p className="reservation-intro">Tell us when you’re coming. We’ll have the table ready.</p>
          <form className="reservation-form" onSubmit={makeReservation}><label>Your name<input name="customerName" autoComplete="name" required /></label><div className="field-row"><label>Email<input type="email" name="email" autoComplete="email" required /></label><label>Phone<input type="tel" name="phone" autoComplete="tel" required /></label></div><div className="field-row"><label>Date & time<input type="datetime-local" name="dateTime" min={new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 16)} required /></label><label>Guests<select name="partySize" defaultValue="2">{Array.from({ length: 10 }, (_, index) => <option key={index + 1} value={index + 1}>{index + 1} {index === 0 ? 'guest' : 'guests'}</option>)}</select></label></div><label>A note for the kitchen <span>OPTIONAL</span><textarea name="notes" rows="2" placeholder="A birthday, a high chair, a favourite corner…" /></label><button className="primary-button checkout-button" disabled={submitting}>{submitting ? 'Saving your table…' : <>Confirm reservation <ArrowRight size={17} /></>}</button></form>
        </section>
      </div>}

      {authOpen && <div className="overlay" onMouseDown={(event) => event.target === event.currentTarget && setAuthOpen(false)}>
        <section className="auth-panel" role="dialog" aria-modal="true" aria-labelledby="auth-title"><div className="panel-heading"><div><span className="eyebrow">A place at our table</span><h2 id="auth-title">{authMode === 'login' ? 'Good to see you again.' : 'Come on in.'}</h2></div><button className="icon-button" onClick={() => setAuthOpen(false)} aria-label="Close sign in"><X /></button></div><p className="reservation-intro">{authMode === 'login' ? 'Sign in to your Spice Garden account.' : 'Create an account for a more personal Spice Garden experience.'}</p>
          <form className="reservation-form" onSubmit={authenticateAccount}>
            {authMode === 'register' && <label>Full name<input name="fullName" autoComplete="name" maxLength="100" required /></label>}
            <label>Email address<input type="email" name="email" autoComplete="email" maxLength="254" required /></label>
            <label>Password<input type="password" name="password" autoComplete={authMode === 'login' ? 'current-password' : 'new-password'} minLength={authMode === 'register' ? 10 : 1} maxLength="72" required />{authMode === 'register' && <span>AT LEAST 10 CHARACTERS</span>}</label>
            {authError && <p className="auth-error" role="alert">{authError}</p>}
            <button className="primary-button checkout-button" disabled={submitting}>{submitting ? 'One moment…' : authMode === 'login' ? <>Sign in <ArrowRight size={17} /></> : <>Create account <ArrowRight size={17} /></>}</button>
          </form>
          <p className="auth-switch">{authMode === 'login' ? 'New to Spice Garden?' : 'Already have an account?'} <button onClick={() => { setAuthMode(authMode === 'login' ? 'register' : 'login'); setAuthError(''); }}>{authMode === 'login' ? 'Create an account' : 'Sign in'}</button></p>
        </section>
      </div>}

      {notice && <div className="notice-overlay"><div className="notice-card" role="alertdialog" aria-modal="true"><span className={notice.title.startsWith('Could not') ? 'notice-icon notice-error' : 'notice-icon'}>{notice.title.startsWith('Could not') ? <X size={20} /> : <Check size={20} />}</span><button className="icon-button notice-close" onClick={() => setNotice(null)} aria-label="Dismiss"><X size={18} /></button><p className="eyebrow">SPICE GARDEN</p><h2>{notice.title}</h2><p>{notice.detail}</p><button className="primary-button" onClick={() => setNotice(null)}>Lovely <ArrowLeft size={16} /></button></div></div>}
    </>
  );
}

export default App;
