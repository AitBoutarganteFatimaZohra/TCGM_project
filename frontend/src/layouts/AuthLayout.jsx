import { Outlet } from 'react-router-dom';

// Les pages publiques (Login) gèrent désormais leur propre mise en page
// plein écran — ce layout ne fait que les laisser passer, sans les
// enfermer dans un conteneur centré à largeur fixe.
const AuthLayout = () => {
  return <Outlet />;
};

export default AuthLayout;