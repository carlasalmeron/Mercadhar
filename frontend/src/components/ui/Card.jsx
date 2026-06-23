const Card = ({ children, className = '', onClick }) => {
  return (
    <div
      className={`${styles.card} ${className} ${onClick ? styles.clickable : ''}`}
      onClick={onClick}
    >
      {children}
    </div>
  );
};

Card.Header = ({ children }) => (
  <div className={styles.header}>{children}</div>
);

Card.Body = ({ children }) => (
  <div className={styles.body}>{children}</div>
);

Card.Footer = ({ children }) => (
  <div className={styles.footer}>{children}</div>
);

import styles from './Card.module.css';
export default Card;