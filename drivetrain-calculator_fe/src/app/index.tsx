/**
 * Home route for the bootstrap app shell. Keep route files thin and delegate to screens.
 */

import { HomeScreen } from '@/features/home/screens/HomeScreen';

export default function IndexRoute() {
  return <HomeScreen />;
}
